/*
 * Copyright (C) 2019 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class MetafileUtilsTest {

    @Test
    public void testFixMessage() {
        Map<Integer, MetafileField> fields = new HashMap<>();
        MetafileField field1 = new MetafileField();
        field1.setName("FIRST FIELD");
        field1.setPropertyName("field1");
        fields.put(1, field1);
        MetafileField field2 = new MetafileField();
        field2.setName("SECOND FIELD");
        field2.setPropertyName("field2");
        fields.put(2, field2);
        MetafileField field3 = new MetafileField();
        field3.setName("TENTH FIELD");
        field3.setPropertyName("field10");
        fields.put(10, field3);
        MetafileEdit edit = new MetafileEdit();
        edit.setFields(fields);

        Assert.assertEquals("MESSAGE", MetafileUtils.fixMessage("MESSAGE", edit, false));
        Assert.assertEquals("MESSAGE FIRST FIELD", MetafileUtils.fixMessage("MESSAGE %F1", edit, false));
        Assert.assertEquals("MESSAGE FIRST FIELD AND SECOND FIELD SOMETHING", MetafileUtils.fixMessage("MESSAGE %F1 AND %F2 SOMETHING", edit, false));
        Assert.assertEquals("MESSAGE SECOND FIELD AND FIRST FIELD SOMETHING", MetafileUtils.fixMessage("MESSAGE %F2 AND %F1 SOMETHING", edit, false));
        Assert.assertEquals("MESSAGE TENTH FIELD", MetafileUtils.fixMessage("MESSAGE %F10", edit, false));
        Assert.assertEquals("MESSAGE TENTH FIELD AND SECOND FIELD SOMETHING", MetafileUtils.fixMessage("MESSAGE %F10 AND %F2 SOMETHING", edit, false));
        Assert.assertEquals("MESSAGE SECOND FIELD AND TENTH FIELD SOMETHING", MetafileUtils.fixMessage("MESSAGE %F2 AND %F10 SOMETHING", edit, false));

        Assert.assertEquals("MESSAGE", MetafileUtils.fixMessage("MESSAGE", edit, false));
        Assert.assertEquals("MESSAGE ${untrimmedline.field1}", MetafileUtils.fixMessage("MESSAGE %V1", edit, false));
        Assert.assertEquals("MESSAGE ${untrimmedline.field1} AND ${untrimmedline.field2} SOMETHING", MetafileUtils.fixMessage("MESSAGE %V1 AND %V2 SOMETHING", edit, false));
        Assert.assertEquals("MESSAGE ${untrimmedline.field2} AND ${untrimmedline.field1} SOMETHING", MetafileUtils.fixMessage("MESSAGE %V2 AND %V1 SOMETHING", edit, false));
        Assert.assertEquals("MESSAGE ${untrimmedline.field10}", MetafileUtils.fixMessage("MESSAGE %V10", edit, false));
        Assert.assertEquals("MESSAGE ${untrimmedline.field10} AND ${untrimmedline.field2} SOMETHING", MetafileUtils.fixMessage("MESSAGE %V10 AND %V2 SOMETHING", edit, false));
        Assert.assertEquals("MESSAGE ${untrimmedline.field2} AND ${untrimmedline.field10} SOMETHING", MetafileUtils.fixMessage("MESSAGE %V2 AND %V10 SOMETHING", edit, false));
    }
    
}
