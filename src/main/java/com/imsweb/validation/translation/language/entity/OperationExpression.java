/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class OperationExpression extends Expression {

    private static final Logger _LOG = LogManager.getLogger(OperationExpression.class);

    private final Expression _exp1;

    private final Expression _exp2;

    private final String _op;

    public OperationExpression(Expression expression1, Expression expression2, String op) {
        _LOG.debug("new OperationExpression(" + expression1 + " " + op + " " + expression2 + ")");

        _exp1 = expression1;
        _exp2 = expression2;
        _op = op;
    }

    public OperationExpression(Expression expression, String op) {
        _LOG.debug("new OperationExpression(" + op + " " + expression + ")");

        _exp1 = expression;
        _exp2 = null;
        _op = op;
    }

    public Expression getLeftExpression() {
        return _exp1;
    }

    public String getOperator() {
        return _op;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {

        // Genedits uses a line precedence which doesn't always agree with the Java AND/OR precedence, so we have to add the parenthesis sometimes...
        StringBuilder exp1Buf = new StringBuilder();
        if (mixesAndOr())
            exp1Buf.append("(");
        _exp1.generateGroovy(exp1Buf, tContext);
        if (mixesAndOr())
            exp1Buf.append(")");
        String exp1Str = exp1Buf.toString();

        String exp2Str = "";
        if (_exp2 != null) {
            StringBuilder exp2Buf = new StringBuilder();
            _exp2.generateGroovy(exp2Buf, tContext);
            exp2Str = exp2Buf.toString();
        }

        // special case, left side is a call to BINLOOKUP, the other side is a boolean constant, replace that boolean by an integer; so something like
        //     if (BINLOOKUP(...) == TRUE)
        // becomes
        //     if (BINLOOKUP(...) == 1)
        if (intCallComparedToBoolConst(_exp1, _exp2, _op)) {
            buf.append(exp1Str).append(" ").append(_op).append(" ").append("true".equals(exp2Str) ? "1" : "0");
        }
        // same as previous case, but inverted; so something like
        //     if (TRUE == BINLOOKUP(...))
        // becomes
        //     if (1 == BINLOOKUP(...))
        else if (boolConstComparedToIntCall(_exp1, _exp2, _op)) {
            buf.append("true".equals(exp1Str) ? "1" : "0").append(" ").append(_op).append(" ").append(exp2Str);
        }
        // special case, left side is a call returning a boolean, the other side is an int constant, remove comparison to constant and negate if needed
        //     if (INLIT(...) == 1)  and  if (INLIST(...) != 0
        // become
        //     if (INLIT(...))
        // and
        //     if (INLIT(...) == 0)  and  if (INLIST(...) != 1
        // become
        //     if (!(INLIT(...)))
        else if (boolCallComparedToIntConst(_exp1, _exp2, _op)) {
            boolean negate = ("==".equals(_op) && "0".equals(exp2Str)) || ("!=".equals(_op) && "1".equals(exp2Str));
            if (negate)
                buf.append("!(");
            buf.append(exp1Str);
            if (negate)
                buf.append(")");
        }
        // same as previous case, but inverted
        else if (intConstComparedToBoolCall(_exp1, _exp2, _op)) {
            boolean negate = ("==".equals(_op) && "0".equals(exp1Str)) || ("!=".equals(_op) && "1".equals(exp1Str));
            if (negate)
                buf.append("!(");
            buf.append(exp2Str);
            if (negate)
                buf.append(")");
        }
        // special case, left side is a boolean variable, other side is 0 or 1; something like
        //   if (boolVar == 1)
        // becomes
        //   if (boolVar == true)
        else if (boolVarComparedToIntConst(_exp1, _exp2, _op, tContext)) {
            buf.append(exp1Str).append(" ").append(_op).append(" ").append("1".equals(exp2Str) ? "true" : "false");
        }
        // same as previous case, but inverted; so something like
        //   if (1 == boolVar)
        // becomes
        //   if (true == boolVar)
        else if (intConstComparedToBoolVar(_exp1, _exp2, _op, tContext)) {
            buf.append("1".equals(exp1Str) ? "true" : "false").append(" ").append(_op).append(" ").append(exp2Str);
        }
        // all other cases...
        else {

            // handle uni-operator (those go in the front of the expression)
            if (_exp2 == null)
                buf.append(_op).append(exp1Str);
            else {
                // special case, cast the division back to an integer
                if ("/".equals(_op))
                    buf.append("(int)(");
                buf.append(exp1Str).append(" ").append(_op).append(" ").append(exp2Str);
                if ("/".equals(_op))
                    buf.append(")");
            }
        }
    }

    private boolean intCallComparedToBoolConst(Expression exp1, Expression exp2, String op) {
        if (exp1 == null || exp2 == null || !Arrays.asList("==", "!=").contains(op))
            return false;

        // deal with outer parenthesis
        Expression e1 = exp1;
        while (e1 instanceof ExpressionWithParenthesis)
            e1 = ((ExpressionWithParenthesis)e1).getExpression();
        Expression e2 = exp2;
        while (e2 instanceof ExpressionWithParenthesis)
            e2 = ((ExpressionWithParenthesis)e2).getExpression();

        return e1.isIntegerCall() && e2.isBooleanConstant();
    }

    private boolean boolConstComparedToIntCall(Expression exp1, Expression exp2, String op) {
        return intCallComparedToBoolConst(exp2, exp1, op);
    }

    private boolean boolCallComparedToIntConst(Expression exp1, Expression exp2, String op) {
        if (exp1 == null || exp2 == null || !Arrays.asList("==", "!=").contains(op))
            return false;

        // deal with outer parenthesis
        Expression e1 = exp1;
        while (e1 instanceof ExpressionWithParenthesis)
            e1 = ((ExpressionWithParenthesis)e1).getExpression();
        Expression e2 = exp2;
        while (e2 instanceof ExpressionWithParenthesis)
            e2 = ((ExpressionWithParenthesis)e2).getExpression();

        return e1.isBooleanCall() && e2.isIntegerLiteral();
    }

    private boolean intConstComparedToBoolCall(Expression exp1, Expression exp2, String op) {
        return boolCallComparedToIntConst(exp2, exp1, op);
    }

    private boolean boolVarComparedToIntConst(Expression exp1, Expression exp2, String op, EditTranslationContext tContext) {
        if (exp1 == null || exp2 == null || !Arrays.asList("==", "!=").contains(op))
            return false;

        // deal with outer parenthesis
        Expression e1 = exp1;
        while (e1 instanceof ExpressionWithParenthesis)
            e1 = ((ExpressionWithParenthesis)e1).getExpression();
        Expression e2 = exp2;
        while (e2 instanceof ExpressionWithParenthesis)
            e2 = ((ExpressionWithParenthesis)e2).getExpression();

        return e1.isBooleanVariable(tContext) && e2.isIntegerLiteral() && Arrays.asList("0", "1").contains(((LiteralExpression)e2).getLiteral());
    }

    private boolean intConstComparedToBoolVar(Expression exp1, Expression exp2, String op, EditTranslationContext tContext) {
        return boolVarComparedToIntConst(exp2, exp1, op, tContext);
    }

    private boolean mixesAndOr() {
        List<String> compOperators = Arrays.asList("==", "!=", ">", ">=", "<", "<=");

        // var1 || var2 && var3
        boolean andMix1 = "&&".equals(_op) && _exp1 instanceof OperationExpression && "||".equals(((OperationExpression)_exp1).getOperator());

        // var1 || var2a == var2b && var3
        boolean andMix2 = "&&".equals(_op) && _exp1 instanceof OperationExpression && compOperators.contains(((OperationExpression)_exp1).getOperator()) &&
                ((OperationExpression)_exp1).getLeftExpression() instanceof OperationExpression && "||".equals(((OperationExpression)((OperationExpression)_exp1).getLeftExpression()).getOperator());

        // var1 && var2 || var3
        boolean orMix1 = "||".equals(_op) && _exp1 instanceof OperationExpression && "&&".equals(((OperationExpression)_exp1).getOperator());

        // var1 && var2a == var2b && var3
        boolean orMix2 = "||".equals(_op) && _exp1 instanceof OperationExpression && compOperators.contains(((OperationExpression)_exp1).getOperator())
                && ((OperationExpression)_exp1).getLeftExpression() instanceof OperationExpression && "&&".equals(
                ((OperationExpression)((OperationExpression)_exp1).getLeftExpression()).getOperator());

        return andMix1 || andMix2 || orMix1 || orMix2;
    }
}
