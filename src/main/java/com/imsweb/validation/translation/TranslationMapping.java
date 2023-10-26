/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

public class TranslationMapping {

    private String _tag;

    private String _name;

    private String _id;

    private String _hash;

    private String _forcedTag;

    public TranslationMapping() {
    }

    public TranslationMapping(String tag, String name, String id, String hash) {
        _tag = tag;
        _name = name;
        _id = id;
        _hash = hash;
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

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getHash() {
        return _hash;
    }

    public void setHash(String hash) {
        _hash = hash;
    }

    public String getForcedTag() {
        return _forcedTag;
    }

    public void setForcedTag(String forcedTag) {
        _forcedTag = forcedTag;
    }
}
