/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;
import com.imsweb.validation.translation.metafile.MetafileField;

public class FieldExpression extends Expression {

    private static final Logger _LOG = LogManager.getLogger(FieldExpression.class);

    private final String _name;

    private final Boolean _standard;

    public FieldExpression(String name, Boolean standard) {
        _LOG.debug("new FieldExpression(" + name + ")");

        _name = name;
        _standard = standard;
    }

    @Override
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {
        if (_standard) {
            // get the field that is being referenced
            MetafileField field = null;
            for (MetafileField f : tContext.getEdit().getFields().values())
                if (f.getName().equalsIgnoreCase(_name)) // apparently they allow different case when referencing fields...
                    field = f;
            if (field == null)
                throw new RuntimeException("Unable to find field '" + _name + "'");
            if (field.getPropertyName() == null)
                throw new RuntimeException("Field '" + _name + "' (#" + field.getNumber() + ") is not associated with a property name!");

            buf.append("untrimmedline.").append(field.getPropertyName());
        }
        else
            throw new RuntimeException("Local field names are not supported!");
    }

}
