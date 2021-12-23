/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;

public class ArgumentList implements ParsedTreeNode {

    private static final Logger _LOG = LogManager.getLogger(ArgumentList.class);

    private final List<Expression> _expressions = new ArrayList<>();

    public ArgumentList() {
        _LOG.debug("new ArgumentList()");
    }

    public ArgumentList(Expression e) {
        _LOG.debug("new ArgumentList(" + e + ")");

        _expressions.add(e);
    }

    public ArgumentList(ArgumentList list, Expression e) {
        _LOG.debug("new ArgumentList(" + list + "," + e + ")");

        _expressions.addAll(list.getExpressions());
        _expressions.add(e);
    }

    public ArgumentList(ArgumentList list) {
        _expressions.addAll(list.getExpressions());
    }

    public List<Expression> getExpressions() {
        return _expressions;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        throw new RuntimeException("This should never be called!");
    }

    @Override
    public String toString() {
        return _expressions.stream().map(Expression::toString).collect(Collectors.toList()).toString();
    }
}
