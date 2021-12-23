/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package demo;

import com.imsweb.validation.translation.MetafileTranslator;
import com.imsweb.validation.translation.TranslationConfiguration;

/**
 * This class demonstrates the full translation of a NAACCR metafile.
 * <br/><br/>
 * To run the demo, create an empty folder on you computer, download the latest NAACCR metafile and copy it into that folder,
 * set the "change_me_full_path" to the path of that folder, adjust the metafile name if needed, and run the class.
 * <br/><br/>
 * Note that this particular version has a syntax error in one of the edits (line 44 of edit 'Autopsy Only, RX, Schema ID, Primary Site, 2018 (NPCR)'
 * has an if statement with 2 else keywords).
 */
public class FullTranslationDemo {

    public static void main(String[] args) throws Exception {

        TranslationConfiguration conf = new TranslationConfiguration();
        conf.setWorkingDirectoryPath("change_me_full_path");
        //conf.setPreviousWorkingDirectoryPath("full_path_previous_translation_dir_if_available");
        conf.setMetafileName("NAACCR_v22A_20210920_XML_layout.smf");
        conf.setTranslationPrefix("NAACCR");
        conf.setNaaccrVersion("220");
        conf.setGenerateGroovySourceCode(true);
        conf.setGroovySourceCodeNumFiles(10); // only needed because NAACCR is a big metafile...

        new MetafileTranslator().executeFullTranslation(conf);
    }
}
