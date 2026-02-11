/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.validation.translation.metafile.MetafileColumnMetaData;
import com.imsweb.validation.translation.metafile.MetafileTable;

public class MetafileTranslatorTest {

    @Test
    public void testGenerateTableExpression() {
        MetafileTable table = new MetafileTable();

        table.setName("T1");
        table.setHeaders(Arrays.asList("H1", "H2"));
        table.setContent(Collections.singletonList(Arrays.asList("V1", "V2")));

        TranslationTable tableUsage = new TranslationTable("T1");
        tableUsage.setTableVarColumns(Collections.singleton("H1"));
        TranslationTableIndex indexUsage = new TranslationTableIndex();
        indexUsage.setName("H2IDX");
        indexUsage.setColumns(Collections.singletonList("H2"));
        tableUsage.setIndexes(Collections.singletonMap(indexUsage.getName(), indexUsage));

        MetafileTranslator translator = new MetafileTranslator();

        Assert.assertNotNull(translator.loadCsStagingInstance(new TranslationConfiguration()));

        // "normal" translation
        Assert.assertEquals("[['H1','H2'],['V1','V2']]", translator.generateTableExpression(table, tableUsage));

        // first column is not used
        tableUsage.setTableVarColumns(Collections.emptySet());
        Assert.assertEquals("[['H2'],['V2']]", translator.generateTableExpression(table, tableUsage));
        tableUsage.setTableVarColumns(Collections.singleton("H1"));

        // second column is not used
        tableUsage.setIndexes(Collections.emptyMap());
        Assert.assertEquals("[['H1'],['V1']]", translator.generateTableExpression(table, tableUsage));
        tableUsage.setIndexes(Collections.singletonMap(indexUsage.getName(), indexUsage));

        // test column types
        MetafileColumnMetaData metadata = new MetafileColumnMetaData();
        metadata.setColumnType("CHAR");
        table.setMetaData(Collections.singletonMap("H1", metadata));
        Assert.assertEquals("[['H1','H2'],['V1','V2']]", translator.generateTableExpression(table, tableUsage));
        metadata.setColumnType("VARCHAR");
        Assert.assertEquals("[['H1','H2'],['V1','V2']]", translator.generateTableExpression(table, tableUsage));
        metadata.setColumnType("INTEGER");
        Assert.assertEquals("[['H1','H2'],[V1,'V2']]", translator.generateTableExpression(table, tableUsage));
    }
}
