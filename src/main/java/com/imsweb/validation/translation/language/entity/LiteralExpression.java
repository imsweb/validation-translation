/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class LiteralExpression extends Expression {

    private static final Logger _LOG = LogManager.getLogger(LiteralExpression.class);

    private final String _literal;

    public LiteralExpression(String literal) {
        _LOG.debug("new LiteralExpression(" + literal + ")");

        _literal = literal;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        if (VariableDeclaration.isDeclaredVariable(_literal)) {
            buf.append(VariableDeclaration.getIdentifierWithCorrectCase(_literal, true));
            if (FunctionStatement.isVariableDeclaredInFunction(_literal, tContext))
                buf.append("_").append(tContext.getCurrentVariableSuffix());
        }
        else
            buf.append(_literal);
    }

    public String getLiteral() {
        return _literal;
    }
}
