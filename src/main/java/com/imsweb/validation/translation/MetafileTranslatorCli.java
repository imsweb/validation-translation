package com.imsweb.validation.translation;

import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MetafileTranslatorCli {
    private static final Logger _LOG = LogManager.getLogger(MetafileTranslatorCli.class);

    public static void main(String[] args) 
        throws Exception {
        CommandLineParser parser = new DefaultParser();


        // set up options
        Options opts = new Options();
        opts.addRequiredOption("p", "path", true, "Path to working directory containing metafile where output will be placed");
        opts.addRequiredOption("c", "config", true, "Filename of configuration properties file within the working directory to use for this translation");
        opts.addOption("h", "help", false, "Prints this help message");
        
        try {
            CommandLine cmdLine = parser.parse(opts, args);
            
            // if help requested, print and exit
            if (cmdLine.hasOption("h")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp(MetafileTranslatorCli.class.getName(), opts);
                return;
            }
            
            // process options into translation configuration
            TranslationConfiguration conf = getConfiguration(cmdLine.getOptionValue("p"), cmdLine.getOptionValue("c"));
            
            // run the translation
            new MetafileTranslator().executeFullTranslation(conf);
        } 
        catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("validation-translation", opts);
        }
    }

    /**
     * Creates a TranslationConfiguration object from the configuration properties file
     * @param path - path to the directory containing the configuration properties file
     * @param filename - name of the configuration properties file
     * @return
     */
    private static TranslationConfiguration getConfiguration(String path, String filename)
            throws IOException, TranslationException {
        // make sure the directory exists
        File configDir = new File(path);
        if (! configDir.exists() || ! configDir.isDirectory()) {
            throw new IOException("Working directory path must point to an existing directory");
        }

        // check to see if the properties file exists
        File configFile = new File(configDir, filename);
        if (! configFile.exists() || ! configFile.isFile()) {
            throw new IOException("Configuration file not found");
        }

        // load the properties file
        Properties configProperties = new Properties();
        try (InputStream inputStream = Files.newInputStream(configFile.toPath())) {
            configProperties.load(inputStream);
        }

        // set up the translation configuration based on contents of the config properties file
        TranslationConfiguration translationConfiguration = new TranslationConfiguration();
        translationConfiguration.setWorkingDirectoryPath(path);

        // load the required items
        if (configProperties.containsKey("metafile-name")) {
            translationConfiguration.setMetafileName(configProperties.getProperty("metafile-name"));
        }
        else {
            throw new TranslationException("No metafile name specified in configuration");
        }

        if (configProperties.containsKey("translation-prefix")) {
            translationConfiguration.setTranslationPrefix(configProperties.getProperty("translation-prefix"));
        }
        else {
            throw new TranslationException("Translation prefix is not specified in configuration");
        }

        if (configProperties.containsKey("naaccr-version")) {
            translationConfiguration.setNaaccrVersion(configProperties.getProperty("naaccr-version"));
        }
        else {
            throw new TranslationException("No NAACCR version specified in configuration");
        }

        // load optional previous translation output path
        if (configProperties.containsKey("previous-working-directory-path")) {
            File previousWorkingDirectoryPath = new File(configProperties.getProperty("previous-working-directory-path"));
            if (! previousWorkingDirectoryPath.exists() || !previousWorkingDirectoryPath.isDirectory()) {
                throw new IOException("Previous working directory specified in configuration file not found");
            }
            translationConfiguration.setPreviousWorkingDirectoryPath(configProperties.getProperty("previous-working-directory-path"));
        }

        // if a field mapping csv file is specified then create a new CsvFieldResolver to handle it
        if (configProperties.containsKey("field-mappings-file")) {
            File fieldMappingsFile = new File(configDir, configProperties.getProperty("field-mappings-file"));
            if (! fieldMappingsFile.exists() || ! fieldMappingsFile.isFile()) {
                throw new IOException("Field mappings file specified in configuration file not found");
            }
            CsvFieldResolver csvFieldResolver = new CsvFieldResolver();
            csvFieldResolver.loadMappingsFile(fieldMappingsFile);

            translationConfiguration.setFieldResolver(csvFieldResolver);
        }

        // if a user dictionary is specified, load it
        if (configProperties.containsKey("user-dictionary-file")) {
            File userDictionaryFile = new File(configDir, configProperties.getProperty("user-dictionary-file"));
            if (! userDictionaryFile.exists() || ! userDictionaryFile.isFile()) {
                throw new IOException("User dictionary file specified in configuration file not found");
            }

            List<NaaccrDictionary> userDictionaries = new ArrayList<NaaccrDictionary>(1);
            NaaccrDictionary dict = NaaccrXmlDictionaryUtils.readDictionary(userDictionaryFile);
            userDictionaries.add(dict);
            _LOG.info("      >> Loaded user dictionary " + userDictionaryFile.getName());

            translationConfiguration.setUserDefinedDictionaries(userDictionaries);
        }

        // check to see if groovy source code needs to be generated
        if (configProperties.containsKey("generate-groovy-src")) {
            // if it is truthy, enable source generation
            if (configProperties.getProperty("generate-groovy-src").equals("1")
                    || configProperties.getProperty("generate-groovy-src").equalsIgnoreCase("yes")
                    || configProperties.getProperty("generate-groovy-src").equalsIgnoreCase("true")) {
                translationConfiguration.setGenerateGroovySourceCode(true);
            }
        }
        // check if groovy source needs to be split
        if (configProperties.containsKey("groovy-src-num-files")) {
            translationConfiguration.setGroovySourceCodeNumFiles(Integer.parseInt(configProperties.getProperty("groovy-src-num-files")));
        }

        return translationConfiguration;
    }
}
