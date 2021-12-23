/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class WhileStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(WhileStatement.class);

    private final Expression _expression;

    private final Statements _statements;

    public WhileStatement(Expression expression, Statements statements) {
        _LOG.debug("new WhileStatement(e, ss)");

        _expression = expression;
        _statements = statements;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {

        StringBuilder exp = new StringBuilder();
        _expression.generateGroovy(exp, tContext);

        StringBuilder stmt = new StringBuilder();
        _statements.generateGroovy(stmt, tContext, indentation + Statement.INDENTATION_STR);

        buf.append(indentation).append("while (").append(exp).append(")");
        if (_statements.getStatements().size() > 1 || !_statements.getStatements().get(0).isOneLineStatementOnly())
            buf.append(" {");
        buf.append("\n").append(stmt);
        if (_statements.getStatements().size() > 1 || !_statements.getStatements().get(0).isOneLineStatementOnly())
            buf.append(indentation).append("}\n");
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return false;
    }
}
