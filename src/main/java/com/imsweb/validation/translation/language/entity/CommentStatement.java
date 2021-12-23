/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class CommentStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(CommentStatement.class);

    public CommentStatement(String comment) {
        _LOG.debug("new CommentStatement(" + comment + ")");
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {
        // I can't get the comment to always be displayed in the right location (with the right block of code) so I think it's better to not write them at all!
        //buf.append(indentation).append(_comment).append("\n");
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return false;
    }
}
