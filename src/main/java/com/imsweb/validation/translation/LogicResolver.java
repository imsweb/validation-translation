/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import com.imsweb.validation.translation.metafile.MetafileEdit;

// tmp fix for bad syntax in NAACCR Call for Data (disabling the fix now, we will see if we still need it during the next translation)
//if ("Height, Date of DX (CER)".equals(edit.getName()) || "Weight, Date of DX (CER)".equals(edit.getName()))
//    editLogic = editLogic.replace("return FAIL:", "return FAIL;");

public class LogicResolver {

    public String resolveLogic(MetafileEdit edit, TranslationConfiguration conf) {
        return edit.getLogic();
    }

}
