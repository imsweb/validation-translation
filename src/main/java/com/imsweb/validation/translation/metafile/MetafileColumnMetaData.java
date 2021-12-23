/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.util.Objects;

public class MetafileColumnMetaData {

    private String _columnName;

    private String _columnType;

    private int _columnSize;

    public String getColumnName() {
        return _columnName;
    }

    public void setColumnName(String columnName) {
        _columnName = columnName;
    }

    public String getColumnType() {
        return _columnType;
    }

    public void setColumnType(String columnType) {
        _columnType = columnType;
    }

    public int getColumnSize() {
        return _columnSize;
    }

    public void setColumnSize(int columnSize) {
        _columnSize = columnSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetafileColumnMetaData that = (MetafileColumnMetaData)o;
        return Objects.equals(_columnName, that._columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_columnName);
    }
}
