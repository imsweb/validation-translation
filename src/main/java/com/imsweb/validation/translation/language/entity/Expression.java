/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.imsweb.validation.translation.EditTranslationContext;
import com.imsweb.validation.functions.MetafileContextFunctions;

public abstract class Expression implements ParsedTreeNode {

    // following Genedits method use an Integer as a returned value but it seems sometimes that results is compared to a boolean constant;
    // in that case, we translate the boolean constant into 1 or 0.
    private static final Set<String> _RETURNS_INTEGER_BUT_USED_AS_BOOLEAN = new HashSet<>();

    static {
        _RETURNS_INTEGER_BUT_USED_AS_BOOLEAN.add("BINLOOKUP");
        _RETURNS_INTEGER_BUT_USED_AS_BOOLEAN.add("YEARINTERNAL_IOP");
        _RETURNS_INTEGER_BUT_USED_AS_BOOLEAN.add("MONTHINTERNAL_IOP");
        _RETURNS_INTEGER_BUT_USED_AS_BOOLEAN.add("DAYINTERNAL_IOP");
    }

    // some methods return a boolean but are set into an "int" variable, can't have that with strong typing...
    private static final Set<String> _RETURNS_BOOL_BUT_ASSIGNED_TO_INT = new HashSet<>();

    static {
        for (Method method : MetafileContextFunctions.class.getDeclaredMethods())
            if (method.getName().startsWith("GEN_") && boolean.class.equals(method.getReturnType()))
                _RETURNS_BOOL_BUT_ASSIGNED_TO_INT.add(method.getName().replace("GEN_", ""));
    }

    // the operators resulting in a boolean expression
    private static final List<String> _BOOLEAN_OPERATORS = Arrays.asList("==", "!=", "&&", "||", ">", ">=", "<", "<=", "!");

    public boolean isIntegerCall() {
        Expression exp = extractExpression();
        return (exp instanceof MethodInvocationExpression) && _RETURNS_INTEGER_BUT_USED_AS_BOOLEAN.contains(((MethodInvocationExpression)exp).getMethodName());
    }

    public boolean isBooleanCall() {
        // a method call, something like "var = INLIST(...)"
        Expression exp = extractExpression();
        return (exp instanceof MethodInvocationExpression && _RETURNS_BOOL_BUT_ASSIGNED_TO_INT.contains(((MethodInvocationExpression)exp).getMethodName()));
    }

    public boolean isBooleanOperation() {
        // result of a boolean expression, something like "var = other_var > 100" or "var = other_var1 && other_var2"
        Expression exp = extractExpression();
        return (exp instanceof OperationExpression && _BOOLEAN_OPERATORS.contains(((OperationExpression)exp).getOperator()));
    }

    public boolean isBooleanConstant() {
        Expression exp = extractExpression();
        return (exp instanceof ConstantExpression && Arrays.asList("TRUE", "FALSE", "PASS", "FAILS").contains(((ConstantExpression)exp).getConstant()));
    }

    public boolean isBooleanVariable(EditTranslationContext tContext) {
        Expression exp = extractExpression();
        return (exp instanceof LiteralExpression) && tContext.getBooleanVariables().contains(((LiteralExpression)exp).getLiteral());
    }

    public boolean isIntegerLiteral() {
        Expression exp = extractExpression();
        return (exp instanceof LiteralExpression) && NumberUtils.isDigits(((LiteralExpression)exp).getLiteral());
    }

    private Expression extractExpression() {
        Expression exp = this;
        while (exp instanceof ExpressionWithParenthesis)
            exp = ((ExpressionWithParenthesis)exp).getExpression();
        return exp;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
