/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.time.LocalDateTime;
import java.util.Objects;

public class MetafileSet {

    private Integer _primaryKey;

    private MetafileAgency _agency;

    private String _tag;

    private String _name;

    private LocalDateTime _lastModified;

    private String _description;

    public Integer getPrimaryKey() {
        return _primaryKey;
    }

    public void setPrimaryKey(Integer primaryKey) {
        _primaryKey = primaryKey;
    }

    public MetafileAgency getAgency() {
        return _agency;
    }

    public void setAgency(MetafileAgency agency) {
        _agency = agency;
    }

    public String getTag() {
        return _tag;
    }

    public void setTag(String tag) {
        _tag = tag;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public LocalDateTime getLastModified() {
        return _lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        _lastModified = lastModified;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetafileSet)) return false;
        MetafileSet that = (MetafileSet)o;
        return Objects.equals(_primaryKey, that._primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_primaryKey);
    }
}
