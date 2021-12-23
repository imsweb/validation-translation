/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.Map;

public class EditTranslationResult {

    private String _groovy;

    // keys are the table names, values are the indexes; fore each index: keys are the index names, values are the list of columns they use
    private Map<String, TranslationTable> _usedTablesAndIndexes;

    public String getGroovy() {
        return _groovy;
    }

    public void setGroovy(String groovy) {
        _groovy = groovy;
    }

    public Map<String, TranslationTable> getUsedTablesAndIndexes() {
        return _usedTablesAndIndexes;
    }

    public void setUsedTablesAndIndexes(Map<String, TranslationTable> usedTablesAndIndexes) {
        _usedTablesAndIndexes = usedTablesAndIndexes;
    }
}
