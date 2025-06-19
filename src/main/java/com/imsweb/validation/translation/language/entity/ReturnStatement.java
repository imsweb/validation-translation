/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;
import com.imsweb.validation.translation.metafile.MetafileMessage;

public class ReturnStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(ReturnStatement.class);

    private final Expression _expression;

    private final String _endOfLineComment;

    public ReturnStatement(Expression expression, String endOfLineComment) {
        _LOG.debug("new ReturnStatement(" + expression + ", " + endOfLineComment + ")");

        _expression = expression;
        _endOfLineComment = endOfLineComment;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {
        buf.append(indentation).append("return ");

        // handle the case where parenthesis would be put around a literal (would be weird, but whatever)
        Expression exp = _expression;
        while (exp instanceof ExpressionWithParenthesis)
            exp = ((ExpressionWithParenthesis)exp).getExpression();

        // Genedits support returning an integer instead of a boolean
        if (exp instanceof LiteralExpression) {
            String literal = ((LiteralExpression)exp).getLiteral();
            if ("TRUE".equalsIgnoreCase(literal) || "1".equalsIgnoreCase(literal) || "WARN".equalsIgnoreCase(literal))
                buf.append("true");
            else if ("FALSE".equalsIgnoreCase(literal) || "0".equalsIgnoreCase(literal))
                buf.append("false");
            else if (literal.matches("\\d+")) {
                MetafileMessage msg = null;
                for (MetafileMessage m : tContext.getEdit().getMessages())
                    if (m.getNumber() != null && m.getNumber().toString().equals(literal))
                        msg = m;
                // I found a bug in some metafile where some edits were not properly linked to their used messages, this addresses that weird case...
                if (msg == null && tContext.getMetafile() != null && tContext.getMetafile().getMessages() != null)
                    msg = tContext.getMetafile().getMessages().stream().filter(m -> m.getNumber().toString().equals(literal)).findFirst().orElse(null);
                if (msg != null)
                    buf.append("Functions.GEN_ERROR_TEXT(binding, '").append(msg.getMessage().replace("'", "\\'")).append("')");
                else
                    throw new RuntimeException("Unable to find message #" + literal + " on " + tContext.getEdit().getName());
            }
            else { // must be a variable
                StringBuilder expBuf = new StringBuilder();
                exp.generateGroovy(expBuf, tContext);
                buf.append(expBuf);
                if (tContext.getIntVariables().contains(literal))
                    buf.append(" == 1");
            }
        }
        else {
            StringBuilder expBuf = new StringBuilder();
            exp.generateGroovy(expBuf, tContext);
            buf.append(expBuf);
        }

        if (_endOfLineComment != null)
            buf.append(" ").append(_endOfLineComment);
        buf.append("\n");
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return true;
    }
}
