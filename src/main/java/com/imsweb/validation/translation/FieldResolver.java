/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryGroupedItem;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;
import com.imsweb.validation.translation.metafile.MetafileField;

@SuppressWarnings("unused")
public class FieldResolver {

    public void resolveField(MetafileField field, TranslationConfiguration conf) throws TranslationException {
        Integer naaccrNumber = field.getNumber();
        if (naaccrNumber == null)
            throw new TranslationException("Unable to find Item Number for field " + field.getName());

        // see if we can resolve the property name before trying to use the dictionary
        String propertyName = resolveFieldPreDictionary(field, conf);

        // if we don't have a property name at this point, let's get it from the NAACCR XML dictionary...
        if (propertyName == null && conf.getNaaccrVersion() != null && NaaccrFormat.isVersionSupported(conf.getNaaccrVersion())) {
            NaaccrDictionaryItem item = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(conf.getNaaccrVersion()).getItemByNaaccrNum(naaccrNumber);
            if (item != null)
                propertyName = item.getNaaccrId();
            else {
                NaaccrDictionaryGroupedItem groupedItem = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(conf.getNaaccrVersion()).getGroupedItemByNaaccrNum(naaccrNumber);
                if (groupedItem != null)
                    propertyName = groupedItem.getNaaccrId();
            }
        }

        // if we don't have a property name yet, try to apply a last step to get it
        if (propertyName == null)
            propertyName = resolveFieldPostDictionary(field, conf);

        field.setPropertyName(propertyName);
    }

    protected String resolveFieldPreDictionary(MetafileField field, TranslationConfiguration conf) {
        return null;
    }

    protected String resolveFieldPostDictionary(MetafileField field, TranslationConfiguration conf) {
        return null;
    }
}
