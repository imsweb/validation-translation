/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import com.imsweb.validation.translation.EditTranslationContext;

public abstract class Statement implements ParsedTreeNode {

    public static final String INDENTATION_STR = "    ";

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        generateGroovy(buf, tContext, "");
    }

    public abstract void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation);

    public abstract boolean isOneLineStatementOnly();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
