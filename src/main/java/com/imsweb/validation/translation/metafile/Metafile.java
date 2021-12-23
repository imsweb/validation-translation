/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.util.List;
import java.util.Objects;

public class Metafile {

    private String _name;

    private String _structureVersion;

    private String _contentVersion;

    private String _comment;

    private List<MetafileAgency> _agencies;

    private List<MetafileMessage> _messages;

    private List<MetafileField> _fields;

    private List<MetafileEdit> _edits;

    private List<MetafileTable> _tables;

    private List<MetafileSet> _sets;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getStructureVersion() {
        return _structureVersion;
    }

    public void setStructureVersion(String structureVersion) {
        _structureVersion = structureVersion;
    }

    public String getContentVersion() {
        return _contentVersion;
    }

    public void setContentVersion(String contentVersion) {
        _contentVersion = contentVersion;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    public List<MetafileAgency> getAgencies() {
        return _agencies;
    }

    public void setAgencies(List<MetafileAgency> agencies) {
        _agencies = agencies;
    }

    public List<MetafileMessage> getMessages() {
        return _messages;
    }

    public void setMessages(List<MetafileMessage> messages) {
        _messages = messages;
    }

    public List<MetafileField> getFields() {
        return _fields;
    }

    public void setFields(List<MetafileField> fields) {
        _fields = fields;
    }

    public List<MetafileEdit> getEdits() {
        return _edits;
    }

    public void setEdits(List<MetafileEdit> edits) {
        _edits = edits;
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
        if (!(o instanceof Metafile)) return false;
        Metafile metafile = (Metafile)o;
        return Objects.equals(_name, metafile._name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name);
    }
}
