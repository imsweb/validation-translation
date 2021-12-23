/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TranslationTableIndex {

    private String _name;

    private List<String> _columns;

    public TranslationTableIndex() {
        _columns = new ArrayList<>();
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public List<String> getColumns() {
        return _columns;
    }

    public void setColumns(List<String> columns) {
        _columns = columns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslationTableIndex that = (TranslationTableIndex)o;
        return Objects.equals(_name, that._name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name);
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean matches(TranslationTableIndex other) {
        // normally two indexes would match if they match on the columns, but it's possible to have a table with an old index based on one column,
        // and some edits using the new SQLLOOKUP calls with the same columns, resulting in different columns, but still the same index.
        //    for example, table SURG03 defines a real index GROUPCODE, but an edits uses "SQLLOOKUP(..., 'GROUP, CODE', ...)

        if (_columns.equals(other.getColumns()))
            return true;

        // only dealing with 2 columns for now!
        if (_columns.size() == 1 && other.getColumns().size() == 2 &&
                (_columns.get(0).equals(other.getColumns().get(0) + other.getColumns().get(1)) ||
                        _columns.get(0).equals(other.getColumns().get(1) + other.getColumns().get(0))))
            return true;

        // reverse
        if (other.getColumns().size() == 1 && _columns.size() == 2 &&
                (other.getColumns().get(0).equals(_columns.get(0) + _columns.get(1)) ||
                        other.getColumns().get(0).equals(_columns.get(1) + _columns.get(0))))
            return true;

        return false;
    }
}
