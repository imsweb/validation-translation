/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.Map;
import java.util.SortedMap;

import com.imsweb.validation.entities.Validator;

public class TranslationResult {

    // new validator
    private Validator _validator;

    // new edits mappings
    private Map<String, TranslationMapping> _newEditsMappings;

    // new sets mappings
    private Map<String, TranslationMapping> _newSetsMappings;

    // new validator properties
    private Map<String, String> _newValidatorProperties;

    // the tables and indexes used in at least one edit
    private SortedMap<String, TranslationTable> _usedTablesAndIndexes;

    // the "old" mapping keyed by edit name
    private Map<String, TranslationMapping> _namesMapping;

    public Validator getValidator() {
        return _validator;
    }

    public void setValidator(Validator validator) {
        _validator = validator;
    }

    public Map<String, TranslationMapping> getNewEditsMappings() {
        return _newEditsMappings;
    }

    public void setNewEditsMappings(Map<String, TranslationMapping> newEditsMappings) {
        _newEditsMappings = newEditsMappings;
    }

    public Map<String, TranslationMapping> getNewSetsMappings() {
        return _newSetsMappings;
    }

    public void setNewSetsMappings(Map<String, TranslationMapping> newSetsMappings) {
        _newSetsMappings = newSetsMappings;
    }

    public Map<String, String> getNewValidatorProperties() {
        return _newValidatorProperties;
    }

    public void setNewValidatorProperties(Map<String, String> newValidatorProperties) {
        _newValidatorProperties = newValidatorProperties;
    }

    public SortedMap<String, TranslationTable> getUsedTablesAndIndexes() {
        return _usedTablesAndIndexes;
    }

    public void setUsedTablesAndIndexes(SortedMap<String, TranslationTable> usedTablesAndIndexes) {
        _usedTablesAndIndexes = usedTablesAndIndexes;
    }

    public Map<String, TranslationMapping> getNamesMapping() {
        return _namesMapping;
    }

    public void setNamesMapping(Map<String, TranslationMapping> namesMapping) {
        _namesMapping = namesMapping;
    }
}
