/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;
import com.imsweb.validation.translation.metafile.MetafileField;

public class FieldResolverTest {

    @Test
    public void testResolveField() throws TranslationException {
        MetafileField field = new MetafileField();
        field.setName("Some Field");
        field.setNumber(1);

        TranslationConfiguration conf = new TranslationConfiguration();
        conf.setNaaccrVersion("210");
        NaaccrDictionaryItem item = new NaaccrDictionaryItem();
        item.setNaaccrId("someField");
        item.setNaaccrNum(1);
        NaaccrDictionary dictionary = new NaaccrDictionary();
        dictionary.setDictionaryUri("SOMETHING");
        dictionary.setNaaccrVersion("210");
        dictionary.setItems(Collections.singletonList(item));
        conf.setUserDefinedDictionaries(Collections.singletonList(dictionary));

        FieldResolver resolver = new FieldResolver();

        // standard item (date of birth)
        field.setNumber(240);
        Assert.assertEquals("dateOfBirth", resolver.resolveField(field, conf));

        // non-standard item
        field.setNumber(1);
        Assert.assertEquals("someField", resolver.resolveField(field, conf));

        // wrong version for the user-defined dictionary
        conf.setNaaccrVersion("220");
        Assert.assertNull(resolver.resolveField(field, conf));
        conf.setNaaccrVersion("210");

        // unknown number
        field.setNumber(10000);
        Assert.assertNull(resolver.resolveField(field, conf));
    }

}
