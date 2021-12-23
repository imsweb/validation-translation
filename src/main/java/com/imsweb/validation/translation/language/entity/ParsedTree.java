/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class ParsedTree implements ParsedTreeNode {

    private static final Logger _LOG = LogManager.getLogger(ParsedTree.class);

    private final Statements _statements;

    public ParsedTree(Statements ss) {
        _LOG.debug("new ParsedTree(" + ss + ")");

        _statements = ss;
    }

    public String translate(EditTranslationContext tContext) {

        // we are replacing the calls to inner functions by the body of the function, so let's put the functions we found during the parsing in the context...
        tContext.setFunctions(FunctionStatement.getFunctions());

        StringBuilder buf = new StringBuilder();
        generateGroovy(buf, tContext);
        return buf.toString().replaceAll("(^\\s+|^\\n+|\\s+$|\\n+$)", "");
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {

        // Genedits uses a lot of context flags and such; we use a Groovy binding for this, so it's important to reset it between edit execution
        buf.append("Functions.GEN_RESET_LOCAL_CONTEXT(binding)\n\n");

        _statements.generateGroovy(buf, tContext);

        // make sure an edit is always returning a boolean result
        boolean stmtMissing = false;
        if (_statements.getStatements().isEmpty())
            stmtMissing = true;
        else {
            for (int i = _statements.getStatements().size() - 1; i >= 0; i--) {
                Statement stmt = _statements.getStatements().get(i);
                if (stmt instanceof EmptyLineStatement || stmt instanceof CommentStatement || stmt instanceof FunctionStatement)
                    continue;
                if (!(stmt instanceof ReturnStatement))
                    stmtMissing = true;
                break;
            }
        }
        if (stmtMissing)
            buf.append("\nreturn true");
    }

    // this is bad, it shouldn't be static and shouldn't need to be reset!
    public static void resetState() {
        DeclarationStatement.resetDeclaredVars();
        VariableDeclaration.resetDeclaredIdentifers();
        FunctionStatement.resetFunctions();
        AssignmentStatement.reset();
        MethodInvocationExpression.resetParsedUsedTables();
    }
}
