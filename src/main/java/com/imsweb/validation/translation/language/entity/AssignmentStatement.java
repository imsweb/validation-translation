/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class AssignmentStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(AssignmentStatement.class);

    private final VariableDeclaration _variable;

    private final Expression _expression;

    private static final Set<String> _BOOLEAN_VARIABLES = new HashSet<>();

    @SuppressWarnings("unused")
    public AssignmentStatement(VariableDeclaration variable, Expression expression, String endOfLineComment) {
        _LOG.debug("ew AssignmentStatement()");

        _variable = variable;
        _expression = expression;

        // special case: keep track of the variables that are assigned with the result of a call to a method returning a boolean value; those variables
        // need to be defined as boolean instead of int
        if (expression.isBooleanCall() || expression.isBooleanOperation() || expression.isBooleanConstant())
            _BOOLEAN_VARIABLES.add(variable.getIdentifier());
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {

        buf.append(indentation);

        buf.append(_variable.getIdentifier());
        if (FunctionStatement.isVariableDeclaredInFunction(_variable.getIdentifier(), tContext))
            buf.append("_").append(tContext.getCurrentVariableSuffix());

        if (_variable.getSize() != null)
            buf.append("[").append(_variable.getSize()).append("]");
        buf.append(" = ");

        StringBuilder expBuf = new StringBuilder();
        _expression.generateGroovy(expBuf, tContext);
        String expStr = expBuf.toString();

        // when looking at expressions, we have to ignore outer parenthesis...
        Expression exp = _expression;
        while (exp instanceof ExpressionWithParenthesis)
            exp = ((ExpressionWithParenthesis)exp).getExpression();

        // Genedits doesn't make the difference between int and boolean, Groovy does; let's transform some expressions to make it work
        if (tContext.getBooleanVariables().contains(_variable.getIdentifier())) {
            if (exp instanceof LiteralExpression && NumberUtils.isDigits(expStr)) {
                if ("0".equals(expStr))
                    buf.append("false");
                else if ("1".equals(expStr))
                    buf.append("true");
                else
                    throw new RuntimeException("Can't assign " + expStr + " to a boolean variable!");
            }
            else
                buf.append(expStr);
        }
        else {
            // Genedits doesn't make the difference between an int and a char, Groovy does; let's add a cast to make it work
            if (DeclarationStatement.getDeclaredCharArrayVars().contains(_variable.getIdentifier()) && exp instanceof LiteralExpression && NumberUtils.isDigits(expStr))
                buf.append("(char)");
            buf.append(expStr);
        }

        // FD - I decided to not include comments anymore, they are too much trouble to get them right...
        //if (_endOfLineComment != null)
        //    buf.append(" ").append(_endOfLineComment);

        buf.append("\n");
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return true;
    }

    public static boolean isBooleanVariable(VariableDeclaration variable) {
        return _BOOLEAN_VARIABLES.contains(variable.getIdentifier());
    }

    public static void reset() {
        _BOOLEAN_VARIABLES.clear();
    }
}
