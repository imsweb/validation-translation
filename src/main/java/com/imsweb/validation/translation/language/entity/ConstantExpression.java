/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class ConstantExpression extends Expression {

    private static final Logger _LOG = LogManager.getLogger(ConstantExpression.class);

    private final String _constantName;

    public ConstantExpression(String constantName) {
        _LOG.debug("new ConstantExpression(" + constantName + ")");

        _constantName = constantName.toUpperCase();
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        if ("PASS".equals(_constantName) || "TRUE".equals(_constantName) || "WARN".equals(_constantName))
            buf.append("true");
        else if ("FAIL".equals(_constantName) || "FALSE".equals(_constantName))
            buf.append("false");
        else if ("DT_TODAY".equals(_constantName))
            buf.append("Functions.GEN_DT_TODAY()");
        else
            buf.append("((Integer)Context.").append(tContext.getValidatorPrefix()).append("_GEN_").append(_constantName.toUpperCase()).append(")");
    }

    public String getConstant() {
        return _constantName;
    }
}
