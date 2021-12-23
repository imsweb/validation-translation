/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;
import com.imsweb.validation.translation.metafile.MetafileColumnMetaData;
import com.imsweb.validation.translation.metafile.MetafileTable;

public class DeclarationStatement extends Statement {

    private static final Logger _LOG = LogManager.getLogger(DeclarationStatement.class);

    private String _type;

    private final List<VariableDeclaration> _variables;

    private final String _endOfLineComment;

    /**
     * Keep track of the special table variables
     */
    private static final Set<String> _TABLE_VARS = new HashSet<>();
    private static final Set<String> _CHAR_ARRAY_VARS = new HashSet<>();

    public DeclarationStatement(String type, List<VariableDeclaration> variables, String endOfLineComment) {
        _LOG.debug("new DeclarationStatement() for " + variables.size() + " variables of type " + type);

        _type = type.toLowerCase();
        if ("long".equals(_type))
            _type = "int";
        if ("tablevar".equals(_type))
            for (VariableDeclaration decl : variables)
                _TABLE_VARS.add(decl.getIdentifier());
        if ("char".equals(_type))
            for (VariableDeclaration decl : variables)
                _CHAR_ARRAY_VARS.add(decl.getIdentifier());
        _variables = variables;
        _endOfLineComment = endOfLineComment;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {
        StringBuilder tmpBuf = new StringBuilder(indentation);

        int numVariablesAdded = 0;

        Set<String> dejaVue = new HashSet<>();

        // keep track of "boolean" int variables that need to be written after the other ones
        List<VariableDeclaration> delayedBooleanVariables = new ArrayList<>();

        // add the type declaration
        if ("tablevar".equals(_type))
            tmpBuf.append("char[]");
        else {
            // handle the array declarations; make sure they are either all arrays, or none are arrays
            boolean oneVarIsArray = false, allVarAreArrays = true;
            for (VariableDeclaration var : _variables) {
                if (var.getSize() != null)
                    oneVarIsArray = true;
                else
                    allVarAreArrays = false;
            }
            if (oneVarIsArray) {
                if (!allVarAreArrays)
                    throw new RuntimeException("Error translating " + tContext.getEdit().getName() + ": found a mix of arrays and non arrays!");
                tmpBuf.append(_type).append("[]");
            }
            else if ("int".equals(_type)) {
                // special case: some "int" variables are assigned the result of a call to a boolean method, let's define those as boolean
                List<VariableDeclaration> booleanVar = new ArrayList<>(), nonBooleanVar = new ArrayList<>();
                for (VariableDeclaration var : _variables) {
                    if (AssignmentStatement.isBooleanVariable(var))
                        booleanVar.add(var);
                    else
                        nonBooleanVar.add(var);
                }

                tContext.getBooleanVariables().addAll(booleanVar.stream().map(VariableDeclaration::getIdentifier).collect(Collectors.toSet()));
                tContext.getIntVariables().addAll(nonBooleanVar.stream().map(VariableDeclaration::getIdentifier).collect(Collectors.toSet()));

                if (!booleanVar.isEmpty()) {
                    if (!nonBooleanVar.isEmpty()) {
                        // the declaration contains a mix of what needs to be "int" and "boolean" variable; let's put the int now and add the boolean later
                        tmpBuf.append(_type);
                        delayedBooleanVariables.addAll(booleanVar);
                    }
                    else
                        tmpBuf.append("boolean");
                }
                else
                    tmpBuf.append(_type);
            }
            else
                tmpBuf.append(_type);
        }
        tmpBuf.append(" ");

        // now that the type has been written, let's right the variable name(s)
        for (VariableDeclaration var : _variables) {
            // Genedit allows multiple variable declaration, which doesn't make any sense; Groovy does not allow that
            if (!dejaVue.contains(var.getIdentifier().toUpperCase()) && !delayedBooleanVariables.contains(var)) {
                dejaVue.add(var.getIdentifier().toUpperCase());
                numVariablesAdded++;

                // this is where the variable name is added
                tmpBuf.append(var.getIdentifier(true));
                if (FunctionStatement.isVariableDeclaredInFunction(var.getIdentifier(), tContext))
                    tmpBuf.append("_").append(tContext.getCurrentVariableSuffix());

                // deal with arrays...
                if (var.getSize() != null)
                    tmpBuf.append(" = new ").append(_type).append("[").append(var.getSize()).append("]");
                else if ("tablevar".equals(_type)) {
                    int maxLength = -1;
                    for (String usedTable : MethodInvocationExpression.getParsedUsedTables()) {
                        MetafileTable table = tContext.getTables() == null ? null : tContext.getTables().get(usedTable);
                        if (table != null) {
                            for (MetafileColumnMetaData data : table.getMetaData().values()) {
                                if (data.getColumnName().equalsIgnoreCase(var.getIdentifier())) {
                                    // this really sucks, but the metafile doesn't report correct column size for triggered-maintained columns (it always returns 1)
                                    if (table.getTriggeredMaintainedIndexes().contains(data.getColumnName()) && data.getColumnSize() == 1)
                                        maxLength = Math.max(maxLength, 100);
                                    else
                                        maxLength = Math.max(maxLength, data.getColumnSize() + 1); // plus one to take into account the end-of-string character
                                    break;
                                }
                            }
                        }
                    }
                    if (maxLength == -1)
                        maxLength = 100; // best we can do...
                    tmpBuf.append(" = new char[").append(maxLength).append("]"); // an optimization here would be to get the maximum possible size from the table...
                }
                tmpBuf.append(", ");
            }
        }
        tmpBuf.setLength(tmpBuf.length() - 2);

        // deal with pending comments
        if (_endOfLineComment != null)
            tmpBuf.append(" ").append(_endOfLineComment);

        tmpBuf.append("\n");

        // deal with the delay variables
        if (!delayedBooleanVariables.isEmpty()) {
            tmpBuf.append("boolean ");
            for (VariableDeclaration var : delayedBooleanVariables) {
                tmpBuf.append(var.getIdentifier()).append(", ");
                numVariablesAdded++;
            }
            tmpBuf.setLength(tmpBuf.length() - 2);
            tmpBuf.append("\n");
        }

        if (numVariablesAdded > 0)
            buf.append(tmpBuf.toString());
    }

    public List<VariableDeclaration> getVariableDeclarations() {
        return _variables;
    }

    @Override
    public boolean isOneLineStatementOnly() {
        return true;
    }

    public static Set<String> getDeclaredTableVars() {
        return _TABLE_VARS;
    }

    public static Set<String> getDeclaredCharArrayVars() {
        return _CHAR_ARRAY_VARS;
    }

    public static void resetDeclaredVars() {
        _TABLE_VARS.clear();
        _CHAR_ARRAY_VARS.clear();
    }
}
