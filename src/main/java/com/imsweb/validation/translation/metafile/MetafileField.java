/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.time.LocalDateTime;
import java.util.Objects;

public class MetafileField {

    private Integer _primaryKey;

    private MetafileAgency _agency;

    private Integer _number;

    private String _name;

    private String _localName;

    private Integer _length;

    private String _type;

    private LocalDateTime _lastModified;

    private String _comments;

    // this is not from the metafile, it's the property that should be used in the translated edits...
    private String _propertyName;

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

    public Integer getNumber() {
        return _number;
    }

    public void setNumber(Integer number) {
        _number = number;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getLocalName() {
        return _localName;
    }

    public void setLocalName(String localName) {
        _localName = localName;
    }

    public Integer getLength() {
        return _length;
    }

    public void setLength(Integer length) {
        _length = length;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public LocalDateTime getLastModified() {
        return _lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        _lastModified = lastModified;
    }

    public String getComments() {
        return _comments;
    }

    public void setComments(String comments) {
        _comments = comments;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    public void setPropertyName(String propertyName) {
        _propertyName = propertyName;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetafileField)) return false;
        MetafileField that = (MetafileField)o;
        return Objects.equals(_primaryKey, that._primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_primaryKey);
    }
}
