/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package demo;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.translation.FieldResolver;
import com.imsweb.validation.translation.MetafileTranslator;
import com.imsweb.validation.translation.TranslationConfiguration;
import com.imsweb.validation.translation.metafile.Metafile;
import com.imsweb.validation.translation.metafile.MetafileField;
import com.imsweb.validation.translation.metafile.MetafileUtils;

/**
 * This class demonstrates translating a given metafile into a Validator object that can be loaded by the validation engine.
 * <br/><br/>
 * To run the demo, create an empty folder on you computer, download the latest NAACCR metafile and copy it into that folder,
 * set the "change_me_full_path" to the path of that folder, adjust the metafile name if needed, and run the class.
 */
public class MetafileTranslationDemo {

    private static final Logger _LOG = LogManager.getLogger(SingleEditTranslationDemo.class);

    public static void main(String[] args) throws Exception {

        String pathToMetafile = "change_me_full_path\\NAACCR_v22A_20210920_XML_layout.smf";

        TranslationConfiguration conf = new TranslationConfiguration();
        conf.setMetafileName(new File(pathToMetafile).getName());
        conf.setTranslationPrefix("NAACCR");
        conf.setNaaccrVersion(NaaccrFormat.NAACCR_VERSION_LATEST);

        // NAACCR metafile references some non-standard fields that the NAACCR XML dictionary won't find...
        conf.setFieldResolver(new FieldResolver() {
            @Override
            protected String resolveFieldPostDictionary(MetafileField field, TranslationConfiguration conf) {
                String propName = super.resolveFieldPostDictionary(field, conf);
                if (propName != null)
                    return propName;

                int itemNumber = field.getNumber();

                if (itemNumber == 10060)
                    return "eodOld4DigitExtent";
                if (itemNumber == 10070)
                    return "eodOld4DigitNodes";
                if (itemNumber == 10080)
                    return "eodOld4DigitSize";
                if (itemNumber == 9980) // EDP MDE Link Variable
                    return "edpMdeLinkVariable";
                if (itemNumber == 9981) // EDP MDE Link Date
                    return "edpMdeLinkData";
                if (itemNumber == 9960) // Height
                    return "height";
                if (itemNumber == 9961) // Weight
                    return "weight";
                if (itemNumber == 9970) // Source Comorbidity
                    return "sourceComorbidity";
                if (itemNumber == 9965) // Tobacco Use Cigarettes
                    return "tobaccoUseCigarettes";
                if (itemNumber == 9968) // Tobacco Use NOS
                    return "tobaccoUseNos";
                if (itemNumber == 9966) // Tobacco Use Other Smoke
                    return "tobaccoUseOtherSmoke";
                if (itemNumber == 9967) // Tobacco Use Smokeless
                    return "tobaccoUseSmokeless";
                if (itemNumber == 9990) // RuralUrban Continuum 2013 (supposed to be added in v15, but it's not there, oh well
                    return "ruralUrbanContinuum2013";

                return null;
            }
        });

        Metafile mf = MetafileUtils.extractMetafileInformation(new File(pathToMetafile));
        _LOG.info("Loaded metafile with " + mf.getEdits().size() + " edits");

        Validator v = new MetafileTranslator().translateMetafile(mf, conf).getValidator();
        _LOG.info("Created new validator '" + v.getId() + "' with " + v.getRules().size() + " edits");
    }
}
