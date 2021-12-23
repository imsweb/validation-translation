/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MetafileTable {

    private Integer _primaryKey;

    private MetafileAgency _agency;

    private String _name;

    private Integer _recordCount;

    private LocalDateTime _lastModified;

    private String _comments;

    private List<String> _headers;

    private Map<String, MetafileColumnMetaData> _metaData;

    private List<List<String>> _content;

    private Map<String, List<String>> _singleColumnIndexes;

    private List<String> _triggeredMaintainedIndexes;

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

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public Integer getRecordCount() {
        return _recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        _recordCount = recordCount;
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

    public List<String> getHeaders() {
        return _headers;
    }

    public void setHeaders(List<String> headers) {
        _headers = headers;
    }

    public Map<String, MetafileColumnMetaData> getMetaData() {
        return _metaData;
    }

    public void setMetaData(Map<String, MetafileColumnMetaData> metaData) {
        _metaData = metaData;
    }

    public List<List<String>> getContent() {
        return _content;
    }

    public void setContent(List<List<String>> content) {
        _content = content;
    }

    public Map<String, List<String>> getSingleColumnIndexes() {
        return _singleColumnIndexes;
    }

    public void setSingleColumnIndexes(Map<String, List<String>> singleColumnIndexes) {
        _singleColumnIndexes = singleColumnIndexes;
    }

    public List<String> getTriggeredMaintainedIndexes() {
        return _triggeredMaintainedIndexes;
    }

    public void setTriggeredMaintainedIndexes(List<String> triggeredMaintainedIndexes) {
        _triggeredMaintainedIndexes = triggeredMaintainedIndexes;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetafileTable)) return false;
        MetafileTable that = (MetafileTable)o;
        return Objects.equals(_primaryKey, that._primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_primaryKey);
    }
}
