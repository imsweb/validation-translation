/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class FunctionStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(FunctionStatement.class);

    private static final Map<String, Statements> _FUNCTIONS = new HashMap<>();

    private static int _FUNCTION_REPLACEMENT_COUNTER = 0;

    public FunctionStatement(String methodName, Statements statements) {
            _LOG.debug("new FunctionStatement() for " + methodName);

            _FUNCTIONS.put(methodName.toUpperCase(), statements);
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {
        // inner-functions are now replaced by their content so there is nothing to write for them
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return false;
    }

    public static Map<String, Statements> getFunctions() {
        return _FUNCTIONS;
    }

    public static int getNextFunctionCounter() {
        return ++_FUNCTION_REPLACEMENT_COUNTER;
    }

    public static void resetFunctions() {
        _FUNCTIONS.clear();
        _FUNCTION_REPLACEMENT_COUNTER = 0;
    }

    /**
     * We want to add a suffix to the variables used inside an inner-function (because we replace the calls to inner-functions by their body content, but that can happen several times, yet
     * we have to keep the variables unique). So we need to use a suffix if there is one available on the context, and the literal we are writing is a variable that was defined in an
     * inner-function.
     */
    public static boolean isVariableDeclaredInFunction(String literal, EditTranslationContext tContext) {

        boolean result = VariableDeclaration.isDeclaredVariable(literal) && tContext.getCurrentVariableSuffix() != null && tContext.getCurrentFunction() != null;
        if (result) {
            boolean declaredInCurrentFunction = false;
            for (Statement stmt : tContext.getFunctions().get(tContext.getCurrentFunction()).getStatements()) {
                if (stmt instanceof DeclarationStatement) {
                    DeclarationStatement declarationStmt = (DeclarationStatement)stmt;
                    for (VariableDeclaration declarationVar : declarationStmt.getVariableDeclarations()) {
                        if (declarationVar.getIdentifier().equalsIgnoreCase(literal)) {
                            declaredInCurrentFunction = true;
                            break;
                        }
                    }
                }
            }
            result = declaredInCurrentFunction;
        }

        return result;
    }
}
