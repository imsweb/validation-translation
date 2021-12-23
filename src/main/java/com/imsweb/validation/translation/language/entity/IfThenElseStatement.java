/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class IfThenElseStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(IfThenElseStatement.class);

    private final Expression _expression;

    private final Statements _s1;

    private final Statements _s2;

    public IfThenElseStatement(Expression expression, Statements s1, Statements s2) {
        _LOG.debug(s2 == null ? ("new IfThenStatement(" + expression + ", " + s1 + ")") : ("new IfThenElseStatement(" + expression + ", " + s1 + ", " + s2 + ")"));

        _expression = expression;
        _s1 = s1;
        _s2 = s2;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {

        StringBuilder expBuf = new StringBuilder();
        _expression.generateGroovy(expBuf, tContext);
        String expStr = expBuf.toString();

        buf.append(indentation).append("if (").append(expStr).append(")");
        if (_s1.getStatements().size() > 1 || !_s1.getStatements().get(0).isOneLineStatementOnly())
            buf.append(" {");

        buf.append("\n");

        StringBuilder s1 = new StringBuilder();
        _s1.generateGroovy(s1, tContext, indentation + Statement.INDENTATION_STR);
        buf.append(s1);
        if (_s1.getStatements().size() > 1 || !_s1.getStatements().get(0).isOneLineStatementOnly())
            buf.append(indentation).append("}\n");

        if (_s2 != null) {
            StringBuilder s2 = new StringBuilder();
            _s2.generateGroovy(s2, tContext, indentation + Statement.INDENTATION_STR);

            buf.append(indentation).append("else");
            if (_s2.getStatements().size() > 1 || !_s2.getStatements().get(0).isOneLineStatementOnly())
                buf.append(" {");
            buf.append("\n").append(s2);
            if (_s2.getStatements().size() > 1 || !_s2.getStatements().get(0).isOneLineStatementOnly())
                buf.append(indentation).append("}\n");
        }
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return false;
    }
}
