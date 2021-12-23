/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.io.File;

import com.imsweb.validation.translation.metafile.Metafile;
import com.imsweb.validation.translation.metafile.MetafileUtils;

public class MetafileResolver {

    public Metafile resolveMetafile(TranslationConfiguration conf) throws TranslationException {
        File metafileFile = new File(conf.getWorkingDirectoryPath(), conf.getMetafileName());
        if (!metafileFile.exists())
            throw new TranslationException("Unbale to find metafile " + metafileFile.getPath());

        return MetafileUtils.extractMetafileInformation(metafileFile);
    }
}
