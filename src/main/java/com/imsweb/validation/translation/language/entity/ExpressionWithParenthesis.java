/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import com.imsweb.validation.translation.EditTranslationContext;

public class ExpressionWithParenthesis extends Expression {

    private final Expression _expression;

    public ExpressionWithParenthesis(Expression expression) {
        _expression = expression;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        StringBuilder exp = new StringBuilder();
        _expression.generateGroovy(exp, tContext);
        buf.append("(").append(exp).append(")");
    }

    public Expression getExpression() {
        return _expression;
    }
}
