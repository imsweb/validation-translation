/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MetafileEdit {

    private Integer _primaryKey;

    private MetafileAgency _agency;

    private String _tag;

    private String _name;

    private MetafileMessage _defaultMessage;

    private List<MetafileMessage> _messages;

    private List<String> _errorMessages;

    private Map<Integer, MetafileField> _fields;

    private LocalDateTime _lastModified;

    private String _description;

    private String _logic;

    private String _adminNotes;

    private List<MetafileTable> _tables;

    private List<MetafileSet> _sets;

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

    public MetafileMessage getDefaultMessage() {
        return _defaultMessage;
    }

    public void setDefaultMessage(MetafileMessage defaultMessage) {
        _defaultMessage = defaultMessage;
    }

    public List<MetafileMessage> getMessages() {
        return _messages;
    }

    public void setMessages(List<MetafileMessage> messages) {
        _messages = messages;
    }

    public List<String> getErrorMessages() {
        return _errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        _errorMessages = errorMessages;
    }

    public Map<Integer, MetafileField> getFields() {
        return _fields;
    }

    public void setFields(Map<Integer, MetafileField> fields) {
        _fields = fields;
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

    public String getLogic() {
        return _logic;
    }

    public void setLogic(String logic) {
        _logic = logic;
    }

    public String getAdminNotes() {
        return _adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        _adminNotes = adminNotes;
    }

    public List<MetafileTable> getTables() {
        return _tables;
    }

    public void setTables(List<MetafileTable> tables) {
        _tables = tables;
    }

    public List<MetafileSet> getSets() {
        return _sets;
    }

    public void setSets(List<MetafileSet> sets) {
        _sets = sets;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetafileEdit)) return false;
        MetafileEdit that = (MetafileEdit)o;
        return Objects.equals(_primaryKey, that._primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_primaryKey);
    }
}
