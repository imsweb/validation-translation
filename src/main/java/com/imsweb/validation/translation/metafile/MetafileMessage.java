/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.time.LocalDateTime;
import java.util.Objects;

public class MetafileMessage {

    private Integer _primaryKey;

    private MetafileAgency _agency;

    private Integer _number;

    private String _message;

    private LocalDateTime _lastModified;

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

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    public LocalDateTime getLastModified() {
        return _lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        _lastModified = lastModified;
    }

    @Override
    public String toString() {
        return _message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetafileMessage)) return false;
        MetafileMessage that = (MetafileMessage)o;
        return Objects.equals(_primaryKey, that._primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_primaryKey);
    }
}
