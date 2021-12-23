/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TranslationTable {

    private String _name;

    private Map<String, TranslationTableIndex> _indexes;

    private Set<String> _tableVarColumns;

    public TranslationTable(String name) {
        _name = name;
        _indexes = new HashMap<>();
        _tableVarColumns = new HashSet<>();
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public Map<String, TranslationTableIndex> getIndexes() {
        return _indexes;
    }

    public void setIndexes(Map<String, TranslationTableIndex> indexes) {
        _indexes = indexes;
    }

    public Set<String> getTableVarColumns() {
        return _tableVarColumns;
    }

    public void setTableVarColumns(Set<String> tableVarColumns) {
        _tableVarColumns = tableVarColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslationTable that = (TranslationTable)o;
        return Objects.equals(_name, that._name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name);
    }
}
