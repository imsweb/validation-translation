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

public class Statements implements ParsedTreeNode {

    private static final Logger _LOG = LogManager.getLogger(Statements.class);

    private final List<Statement> _statements = new ArrayList<>();

    public Statements(Statement s) {
        _LOG.debug("new Statements(" + s + ")");

        _statements.add(s);
    }

    public Statements(Statement s, Statements ss) {
        _LOG.debug("new Statements(" + s + ", " + ss + ")");

        _statements.add(s);
        if (ss != null)
            _statements.addAll(ss.getStatements());
    }

    public Statements(Statements ss, Statement s) {
        _LOG.debug("new Statements(" + ss + ", " + s + ")");

        if (ss != null)
            _statements.addAll(ss.getStatements());
        _statements.add(s);
    }

    public Statements(Statements ss) {
        _LOG.debug("new Statements(" + ss + ")");

        _statements.addAll(ss.getStatements());
    }

    public Statements(Statements opt1, Statement s, Statements opt2) {
        _LOG.debug("new Statements(" + opt1 + ", " + s + "," + opt2 + ")");

        // This is done this way because the CUP definition is not done properly; consider this statement:
        //    if (expression)
        //        return PASS;
        //
        //    return FALSE;
        // our current definition will combine the return PASS statement and the empty-line statement together under the IF; this is not correct, instead the
        // empty line statement should be part of the outer statement. This is obvious because the if has no braces!

        if (opt1 != null)
            for (Statement stmt : opt1.getStatements())
                if (!(stmt instanceof EmptyLineStatement))
                    _statements.add(stmt);

        _statements.add(s);

        if (opt2 != null)
            for (Statement stmt : opt2.getStatements())
                if (!(stmt instanceof EmptyLineStatement))
                    _statements.add(stmt);
    }

    public Statements(Statements opt1, Statements ss, Statements opt2) {
        _LOG.debug("new Statements(" + opt1 + ", " + ss + "," + opt2 + ")");

        // This is done this way because the CUP definition is not done properly; consider this statement:
        //    if (expression)
        //        return PASS;
        //
        //    return FALSE;
        // our current definition will combine the return PASS statement and the empty-line statement together under the IF; this is not correct, instead the
        // empty line statement should be part of the outer statement. This is obvious because the if has no braces!

        if (opt1 != null)
            for (Statement stmt : opt1.getStatements())
                if (!(stmt instanceof EmptyLineStatement))
                    _statements.add(stmt);

        _statements.addAll(ss.getStatements());

        if (opt2 != null)
            for (Statement stmt : opt2.getStatements())
                if (!(stmt instanceof EmptyLineStatement))
                    _statements.add(stmt);
    }

    public List<Statement> getStatements() {
        return _statements;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        generateGroovy(buf, tContext, "");
    }

    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext, String indentation) {
        for (Statement s : _statements)
            s.generateGroovy(buf, tContext, indentation);
    }

    @Override
    public String toString() {
        return _statements.stream().map(Statement::toString).collect(Collectors.toList()).toString();
    }
}
