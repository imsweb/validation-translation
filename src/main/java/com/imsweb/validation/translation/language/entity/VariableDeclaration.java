/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class VariableDeclaration implements ParsedTreeNode {

    private static final Logger _LOG = LogManager.getLogger(VariableDeclaration.class);

    private static final List<String> _FORBIDDEN = Arrays.asList("short", "int", "long", "bool", "boolean");

    private final String _identifier;

    private final Integer _size;

    /**
     * Groovy is case sensitive, Genedits isn't; this is a major issue for the variables. So I am keeping track of the declared variable here
     * and everytime an identifier is written, I am using the same case as the one defined here!
     */
    private static final Set<String> _DECLARED_IDENTIFIERS = new HashSet<>();

    public VariableDeclaration(String identifier) {
        this(identifier, null);
    }

    public VariableDeclaration(String identifier, Integer size) {
        _LOG.debug("new VariableDeclaration() for " + identifier);

        _identifier = identifier;
        _size = size;

        // register this identifier
        boolean found = false;
        for (String s : _DECLARED_IDENTIFIERS) {
            if (s.equalsIgnoreCase(identifier)) {
                found = true;
                break;
            }
        }
        if (!found)
            _DECLARED_IDENTIFIERS.add(identifier);
    }

    public String getIdentifier() {
        return getIdentifier(false);
    }

    public String getIdentifier(boolean handleForbiddenKeyword) {
        return getIdentifierWithCorrectCase(_identifier, handleForbiddenKeyword);
    }

    public Integer getSize() {
        return _size;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        throw new RuntimeException("This method should never be called");
    }

    public static boolean isDeclaredVariable(String rawIdentifier) {
        for (String s : _DECLARED_IDENTIFIERS)
            if (s.equalsIgnoreCase(rawIdentifier))
                return true;
        return false;
    }

    public static String getIdentifierWithCorrectCase(String rawIdentifier, boolean handleForbiddenKeyword) {
        for (String s : _DECLARED_IDENTIFIERS)
            if (s.equalsIgnoreCase(rawIdentifier))
                return handleForbiddenKeyword && _FORBIDDEN.contains(s.toLowerCase()) ? (s + "_not_the_keyword") : s;
        return rawIdentifier;
    }

    public static void resetDeclaredIdentifers() {
        _DECLARED_IDENTIFIERS.clear();
    }

    @Override
    public String toString() {
        return _identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableDeclaration that = (VariableDeclaration)o;
        return Objects.equals(_identifier, that._identifier) &&
                Objects.equals(_size, that._size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_identifier, _size);
    }
}
