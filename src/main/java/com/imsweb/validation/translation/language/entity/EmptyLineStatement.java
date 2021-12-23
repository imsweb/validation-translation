/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class EmptyLineStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(EmptyLineStatement.class);

    public EmptyLineStatement() {
        _LOG.debug("new EmptyLineStatement()");
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {
        buf.append("\n");
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return true;
    }
}
