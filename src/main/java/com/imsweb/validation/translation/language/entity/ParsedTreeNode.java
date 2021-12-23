/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import com.imsweb.validation.translation.EditTranslationContext;

public interface ParsedTreeNode {

    void generateGroovy(StringBuilder buf, EditTranslationContext tContext);
}
