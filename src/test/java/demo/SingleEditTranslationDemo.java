/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package demo;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.validation.translation.EditTranslationResult;
import com.imsweb.validation.translation.MetafileTranslator;
import com.imsweb.validation.translation.TranslationConfiguration;
import com.imsweb.validation.translation.metafile.Metafile;
import com.imsweb.validation.translation.metafile.MetafileEdit;
import com.imsweb.validation.translation.metafile.MetafileUtils;

/**
 * This demo class can be used to translate a single edit (a common use-case is when the full translation fails to translate a given edit).
 * <br/><br/>
 * To run the demo, create an empty folder on you computer, download the latest NAACCR metafile and copy it into that folder,
 * set the "change_me_full_path" to the path of that folder, adjust the metafile name if needed, and run the class.
 * <br/><br/>
 * This class also allows tweaking the edit logic on-the-fly; that can be used to simplify the logic and identify what part of it is the actual problem.
 * To do that, just copy the content of the logic in the "single-edit-to-translate.txt" file and uncomment the line that sets the logic.
 */
public class SingleEditTranslationDemo {

    private static final Logger _LOG = LogManager.getLogger(SingleEditTranslationDemo.class);

    public static void main(String[] args) throws Exception {

        String pathToMetafile = "change_me_full_path\\NAACCR_v22A_20210920_XML_layout.smf";

        String editName = "Abstracted By (COC)";

        // we don't need to set a lot of configuration properties, but the naaccr version is needed to properly derive the property names
        TranslationConfiguration conf = new TranslationConfiguration();
        conf.setNaaccrVersion(NaaccrFormat.NAACCR_VERSION_LATEST);

        Metafile mf = MetafileUtils.extractMetafileInformation(new File(pathToMetafile));
        MetafileEdit edit = mf.getEdits().stream().filter(e -> e.getName().equals(editName)).findFirst().orElse(null);
        if (edit == null)
            throw new RuntimeException("Unable to find " + editName);

        //edit.setLogic(SeerUtils.readUrl(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("single-edit-to-translate.txt"))));

        _LOG.info("\r\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>  START METAFILE LOGIC  >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        _LOG.info(edit.getLogic());
        _LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>  END METAFILE LOGIC  >>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        // to see this debug info information, set level to debug in log4j conf file
        _LOG.debug("\r\n\r\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>  START EXTRA TRANSLATION INFO  >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        EditTranslationResult output = new MetafileTranslator().translateEdit(edit, mf, conf);
        _LOG.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>  END EXTRA TRANSLATION INFO  >>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        _LOG.info("\r\n\r\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>  START TRANSLATED LOGIC  >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        _LOG.info(output.getGroovy());
        _LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>  END TRANSLATED LOGIC  >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
}
