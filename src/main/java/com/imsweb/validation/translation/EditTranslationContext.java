/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.imsweb.validation.translation.language.entity.Statements;
import com.imsweb.validation.translation.metafile.MetafileEdit;
import com.imsweb.validation.translation.metafile.MetafileTable;

public class EditTranslationContext {

    // the edit being translated
    private final MetafileEdit _edit;

    // the validator prefix (used to know what metafile we are translating)
    private final String _validatorPrefix;

    // the available tables in the metafile
    private final Map<String, MetafileTable> _tables;

    // the used tables and indexes, this is populated as part of the translation (keys are the table name)
    private final Map<String, TranslationTable> _usedTablesAndIndexes;

    // the defined "boolean" variables
    private final Set<String> _booleanVariables;

    // the defined "boolean" variables (not the arrays, the pure int)
    private final Set<String> _intVariables;

    // the defined inner functions
    private Map<String, Statements> _functions;

    // the current inner-function that is being written
    private String _currentFunction;

    // when copying the text of an inner function, we have to make sure the declared variables are unique...
    private String _currentVariableSuffix;

    // sigh...
    private String _currentFunctionIndentation;

    /**
     * Constructor.
     */
    public EditTranslationContext(MetafileEdit mfEdit, String mfPrefix, Map<String, MetafileTable> tables) {
        _edit = mfEdit;
        _validatorPrefix = mfPrefix;
        _tables = tables;
        _usedTablesAndIndexes = new HashMap<>();
        _booleanVariables = new HashSet<>();
        _intVariables = new HashSet<>();
    }

    public String getValidatorPrefix() {
        return _validatorPrefix;
    }

    public MetafileEdit getEdit() {
        return _edit;
    }

    public Map<String, MetafileTable> getTables() {
        return _tables;
    }

    public Map<String, TranslationTable> getUsedTablesAndIndexes() {
        return _usedTablesAndIndexes;
    }

    public Set<String> getBooleanVariables() {
        return _booleanVariables;
    }

    public Set<String> getIntVariables() {
        return _intVariables;
    }

    public void setFunctions(Map<String, Statements> functions) {
        _functions = functions;
    }

    public Map<String, Statements> getFunctions() {
        return _functions;
    }

    public String getCurrentFunction() {
        return _currentFunction;
    }

    public void setCurrentFunction(String currentFunction) {
        _currentFunction = currentFunction;
    }

    public String getCurrentVariableSuffix() {
        return _currentVariableSuffix;
    }

    public void setCurrentVariableSuffix(String currentVariableSuffix) {
        _currentVariableSuffix = currentVariableSuffix;
    }

    public String getCurrentFunctionIndentation() {
        return _currentFunctionIndentation;
    }

    public void setCurrentFunctionIndentation(String currentFunctionIndentation) {
        _currentFunctionIndentation = currentFunctionIndentation;
    }
}
