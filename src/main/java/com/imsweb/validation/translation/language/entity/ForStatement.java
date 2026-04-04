/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class ForStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(ForStatement.class);

    private final AssignmentStatement _lefAssignment;

    private final Expression _expression;

    private final AssignmentStatement _rightAssignment;


    private final Statements _statements;

    public ForStatement(VariableDeclaration declaration1, Expression expression1, Expression expression2, VariableDeclaration declaration3, Expression expression3, Statements statements) {
        _LOG.debug("new ForStatement(i1, e1, e2, i3, e3, ss)");

        _lefAssignment = new AssignmentStatement(declaration1, expression1, null, false);
        _expression = expression2;
        _rightAssignment = new AssignmentStatement(declaration3, expression3, null, false);
        _statements = statements;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {

        StringBuilder left = new StringBuilder();
        _lefAssignment.generateGroovy(left, tContext);

        StringBuilder exp = new StringBuilder();
        _expression.generateGroovy(exp, tContext);

        StringBuilder right = new StringBuilder();
        _rightAssignment.generateGroovy(right, tContext);

        StringBuilder stmt = new StringBuilder();
        _statements.generateGroovy(stmt, tContext, indentation + Statement.INDENTATION_STR);

        buf.append(indentation).append("for (").append(left).append("; ").append(exp).append("; ").append(right).append(")");
        if (_statements.getStatements().size() > 1 || !_statements.getStatements().getFirst().isOneLineStatementOnly())
            buf.append(" {");
        buf.append("\n").append(stmt);
        if (_statements.getStatements().size() > 1 || !_statements.getStatements().getFirst().isOneLineStatementOnly())
            buf.append(indentation).append("}\n");
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return false;
    }
}
