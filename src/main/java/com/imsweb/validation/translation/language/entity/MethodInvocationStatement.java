/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class MethodInvocationStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(MethodInvocationStatement.class);

    private final String _methodName;

    private final ArgumentList _arguments;

    private final String _endOfLineComment;

    public MethodInvocationStatement(String endOfLineComment) {
        // in my opinion, an edit that uses empty statements is just very poorly written. Anyway, I find the empty statement blocks
        // hard to read, and I am replacing them here with a dummy call...
        this("NOOP", new ArgumentList(), endOfLineComment);
    }

    public MethodInvocationStatement(String methodName, ArgumentList arguments, String endOfLineComment) {
        _LOG.debug("new MethodInvocationStatement(" + methodName + ", " + arguments + ", " + endOfLineComment + ")");

        _methodName = methodName;
        _arguments = arguments;
        _endOfLineComment = endOfLineComment;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {

        // inner-functions (non-internal) will deal with their indentation themselves...
        if (MethodInvocationExpression.isInternalMethod(_methodName))
            buf.append(indentation);
        else
            tContext.setCurrentFunctionIndentation(indentation);

        // the logic to actually translate a method call is in the expression already, so let's share that logic!
        new MethodInvocationExpression(_methodName, _arguments).generateGroovy(buf, tContext);

        tContext.setCurrentFunctionIndentation(null);

        if (_endOfLineComment != null)
            buf.append(" ").append(_endOfLineComment);
        buf.append("\n");
    }

    @Override
    public boolean isOneLineStatementOnly() {
        // calls to inner-functions are replaced by their content (usually more than one statement), so only internal calls are single-line statements...
        return MethodInvocationExpression.isInternalMethod(_methodName);
    }

}
