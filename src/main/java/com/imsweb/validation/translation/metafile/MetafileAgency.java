/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.time.LocalDateTime;
import java.util.Objects;

public class MetafileAgency {

    private Integer _primaryKey;

    private String _adminCode;

    private String _organizationName;

    private LocalDateTime _lastModified;

    private String _comments;

    public Integer getPrimaryKey() {
        return _primaryKey;
    }

    public void setPrimaryKey(Integer primaryKey) {
        _primaryKey = primaryKey;
    }

    public String getAdminCode() {
        return _adminCode;
    }

    public void setAdminCode(String adminCode) {
        _adminCode = adminCode;
    }

    public String getOrganizationName() {
        return _organizationName;
    }

    public void setOrganizationName(String organizationName) {
        _organizationName = organizationName;
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

    @Override
    public String toString() {
        return _adminCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetafileAgency)) return false;
        MetafileAgency that = (MetafileAgency)o;
        return Objects.equals(_primaryKey, that._primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_primaryKey);
    }
}
