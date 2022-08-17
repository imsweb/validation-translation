/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.seerutils.SeerUtils;
import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.InitializationOptions;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.ValidationXmlUtils;
import com.imsweb.validation.entities.Category;
import com.imsweb.validation.entities.ContextEntry;
import com.imsweb.validation.entities.EditableValidator;
import com.imsweb.validation.entities.EmbeddedSet;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.entities.ValidatorRelease;
import com.imsweb.validation.entities.ValidatorVersion;
import com.imsweb.validation.functions.MetafileContextFunctions;
import com.imsweb.validation.translation.language.GeneditsLexer;
import com.imsweb.validation.translation.language.GeneditsParser;
import com.imsweb.validation.translation.language.entity.ParsedTree;
import com.imsweb.validation.translation.metafile.Metafile;
import com.imsweb.validation.translation.metafile.MetafileAgency;
import com.imsweb.validation.translation.metafile.MetafileColumnMetaData;
import com.imsweb.validation.translation.metafile.MetafileEdit;
import com.imsweb.validation.translation.metafile.MetafileField;
import com.imsweb.validation.translation.metafile.MetafileSet;
import com.imsweb.validation.translation.metafile.MetafileTable;
import com.imsweb.validation.translation.metafile.MetafileUtils;

import static com.imsweb.validation.ValidationEngine.CONTEXT_TYPE_JAVA;
import static com.imsweb.validation.ValidationEngine.CONTEXT_TYPE_TABLE;
import static com.imsweb.validation.ValidationEngine.CONTEXT_TYPE_TABLE_INDEX_DEF;

/**
 * Use this file to translate Genedits metafiles (SMF).
 * <br/><br/>
 * See the readme file of the project for more information on how to use the class.
 */
@SuppressWarnings({"StringBufferReplaceableByString", "UnusedReturnValue", "java:S2629", "java:S3457"})
public class MetafileTranslator {

    private static final Logger _LOG = LogManager.getLogger(MetafileTranslator.class);

    private static final Map<String, String> _GENEDITS_CONSTANTS = new HashMap<>();

    static {
        // the pass/fail/true/false are translated into boolean, no need for the constants
        _GENEDITS_CONSTANTS.put("GEN_RIGHT", "0");
        _GENEDITS_CONSTANTS.put("GEN_LEFT", "1");
        _GENEDITS_CONSTANTS.put("GEN_BOTH", "2");
        _GENEDITS_CONSTANTS.put("GEN_DT_VALID", "1342177279");
        _GENEDITS_CONSTANTS.put("GEN_DT_MISSING", "1610612735");
        _GENEDITS_CONSTANTS.put("GEN_DT_ERROR", "1879048191");
        _GENEDITS_CONSTANTS.put("GEN_DT_UNKNOWN", "2147483647");
        _GENEDITS_CONSTANTS.put("GEN_DT_EMPTY", "1073741823");
        _GENEDITS_CONSTANTS.put("GEN_DT_DAY_EMPTY", "805306367");
        _GENEDITS_CONSTANTS.put("GEN_DT_MONTH_EMPTY", "536870911");
        _GENEDITS_CONSTANTS.put("GEN_DT_MIN", "1");
        _GENEDITS_CONSTANTS.put("GEN_DT_MAX", "2");
        _GENEDITS_CONSTANTS.put("GEN_DT_EXACT", "3");
    }

    /**
     * Created on Aug 26, 2010 by depryf
     */
    public TranslationResult executeFullTranslation(TranslationConfiguration conf) throws IOException, TranslationException {
        validateConfiguration(conf, true);

        File workingDir = new File(conf.getWorkingDirectoryPath());
        File outputDir = createOrEmptyDir(new File(workingDir, conf.getOutputDirectoryName()));

        // read previous translation output to use as input (it's OK for that directory to not exist)
        PreviousTranslationInfo previousInfo = new PreviousTranslationInfo(conf);

        // log a few things
        _LOG.info("Starting translation...");
        _LOG.info("  > working folder: " + workingDir.getPath());
        _LOG.info("  > metafile: " + conf.getMetafileName() + " (previously " + previousInfo.getPreviousAttribute(ValidationXmlUtils.ROOT_ATTR_TRANSLATED_FROM) + ")");
        _LOG.info("  > version: " + evaluateNewValidatorVersion(conf, previousInfo) + " (previously " + previousInfo.getPreviousAttribute(ValidationXmlUtils.ROOT_ATTR_VERSION) + ")");
        if (conf.getMinValidationEnginVersion() != null)
            _LOG.info(
                    "  > min engine version: " + conf.getMinValidationEnginVersion() + " (previously " + previousInfo.getPreviousAttribute(ValidationXmlUtils.ROOT_ATTR_MIN_ENGINE_VERSION) + ")");
        if (conf.getNaaccrVersion() != null)
            _LOG.info("  > NAACCR version: " + conf.getNaaccrVersion());

        // load the metafile
        Metafile mf = conf.getMetafileResolver().resolveMetafile(conf);

        // initialize a bunch of stuff
        _LOG.info("\r\nInitializing inputs...");
        ValidationServices.initialize(new ValidationServices());
        ValidationContextFunctions.initialize(new MetafileContextFunctions(Staging.getInstance(CsDataProvider.getInstance(CsVersion.LATEST)), null, null));
        _LOG.info("  > initialized Collaborative Stage " + CsVersion.LATEST.getVersion());
        InitializationOptions options = new InitializationOptions();
        options.setNumCompilationThreads(conf.getNumCompilationThreads());
        ValidationEngine.getInstance().initialize(options);
        _LOG.info("  > initialized validation engine v" + ValidationEngine.getInstance().getEngineVersion());
        NaaccrDictionary dictionary = conf.getNaaccrVersion() != null ? NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(conf.getNaaccrVersion()) : null;
        if (dictionary != null)
            _LOG.info("  > initialized " + (dictionary.getItems().size() + dictionary.getGroupedItems().size()) + " NAACCR fields");
        _LOG.info("  > initialized metafile [" + mf.getAgencies().size() + " agencies, " + mf.getEdits().size() + " edits, " + mf.getTables().size() + " tables, " + mf.getSets().size() + " sets]");
        if (!previousInfo.getAllEditsTagInPreviousMapping().isEmpty())
            _LOG.info("  > found " + previousInfo.getAllEditsTagInPreviousMapping().size() + " edits in previous translation mapping...");
        else
            _LOG.info("  > no previous edits mapping detected, the new IDs will be assigned from scratch...");
        if (!previousInfo.getAllSetsTagInPreviousMapping().isEmpty())
            _LOG.info("  > found " + previousInfo.getAllSetsTagInPreviousMapping().size() + " sets in previous translation mapping...");
        else
            _LOG.info("  > no previous sets mapping detected, the new IDs will be assigned from scratch...");

        // create the validator
        _LOG.info("\r\nTranslating...");
        TranslationResult result = createValidator(mf, conf, previousInfo);

        // output a few more messages
        Validator v = result.getValidator();
        _LOG.info("  > added " + v.getRawContext().size() + " context entries (includes " + _GENEDITS_CONSTANTS.size() + " Genedits constants)");
        if (!result.getUsedTablesAndIndexes().isEmpty()) {
            StringBuilder msg = new StringBuilder("  > following " + result.getUsedTablesAndIndexes().size() + " tables and indexes were added as contexts:\n");
            for (String tableName : result.getUsedTablesAndIndexes().keySet())
                msg.append("      >> ").append(tableName).append("\n");
            msg.setLength(msg.length() - 1);
            _LOG.info(msg.toString());
        }
        SortedSet<String> ignoredTableNames = mf.getTables().stream().map(MetafileTable::getName).collect(Collectors.toCollection(TreeSet::new));
        ignoredTableNames.removeAll(result.getUsedTablesAndIndexes().keySet());
        if (!ignoredTableNames.isEmpty()) {
            StringBuilder msg = new StringBuilder("  > following " + ignoredTableNames.size() + " tables were ignored because not actually called by any edit:\n");
            for (String ignoredTableName : ignoredTableNames)
                msg.append("      >> ").append(ignoredTableName).append("\n");
            msg.setLength(msg.length() - 1);
            _LOG.info(msg.toString());
        }

        // display agencies info
        Map<String, Set<String>> tmpMap = new TreeMap<>();
        for (Rule r : v.getRules()) {
            Set<String> tmpSet = tmpMap.computeIfAbsent(r.getAgency().toUpperCase(), k -> new HashSet<>());
            tmpSet.add(r.getId());
        }
        StringBuilder msg = new StringBuilder("  > found " + tmpMap.size() + " agencies:\n");
        for (Entry<String, Set<String>> entry : tmpMap.entrySet())
            msg.append("      >> ").append(entry.getKey()).append(" (owning ").append(entry.getValue().size()).append(" edits)\n");
        msg.setLength(msg.length() - 1);
        _LOG.info(msg.toString());

        // display edits info
        _LOG.info("  > translated " + v.getRules().size() + " edits");

        List<TranslationMapping> unchanged = previousInfo.extractIdentical(previousInfo.getAllPreviousEditsMapping(), result.getNewEditsMappings());
        unchanged.sort(Comparator.comparing(TranslationMapping::getId));
        if (!unchanged.isEmpty()) {
            msg = new StringBuilder("  > following " + unchanged.size() + " edits existed in both versions and were identical:\n");
            for (TranslationMapping mapping : unchanged)
                msg.append("      >> ").append(mapping.getId()).append(" -> [").append(mapping.getTag()).append("] ").append(mapping.getName()).append("\n");
            msg.setLength(msg.length() - 1);
            _LOG.info(msg.toString());
        }
        else
            _LOG.info("  > no edits were identical in both versions");

        List<TranslationMapping> modified = previousInfo.extractModified(previousInfo.getAllPreviousEditsMapping(), result.getNewEditsMappings());
        modified.sort(Comparator.comparing(TranslationMapping::getId));
        if (!modified.isEmpty()) {
            msg = new StringBuilder("  > following " + modified.size() + " edits got modified:\n");
            for (TranslationMapping mapping : modified)
                msg.append("      >> ").append(mapping.getId()).append(" -> [").append(mapping.getTag()).append("] ").append(mapping.getName()).append("\n");
            msg.setLength(msg.length() - 1);
            _LOG.info(msg.toString());
        }
        else
            _LOG.info("  > no edit got modified");

        List<TranslationMapping> added = previousInfo.extractAdded(previousInfo.getAllPreviousEditsMapping(), result.getNewEditsMappings());
        added.sort(Comparator.comparing(TranslationMapping::getId));
        if (!added.isEmpty()) {
            msg = new StringBuilder("  > following " + added.size() + " edits got added:\n");
            for (TranslationMapping mapping : added)
                msg.append("      >> ").append(mapping.getId()).append(" -> [").append(mapping.getTag()).append("] ").append(mapping.getName()).append("\n");
            msg.setLength(msg.length() - 1);
            _LOG.info(msg.toString());
        }
        else
            _LOG.info("  > no edit got added");

        List<TranslationMapping> removed = previousInfo.extractRemoved(previousInfo.getAllPreviousEditsMapping(), result.getNewEditsMappings());
        removed.sort(Comparator.comparing(TranslationMapping::getId));
        if (!removed.isEmpty()) {
            msg = new StringBuilder("  > following " + removed.size() + " edits got removed:\n");
            for (TranslationMapping mapping : removed)
                msg.append("      >> ").append(mapping.getId()).append(" -> [").append(mapping.getTag()).append("] ").append(mapping.getName()).append("\n");
            msg.setLength(msg.length() - 1);
            _LOG.info(msg.toString());
        }
        else
            _LOG.info("  > no edit got removed");

        // display sets info
        if (v.getSets().isEmpty())
            _LOG.info("  > translated no sets");
        else {
            _LOG.info("  > translated " + v.getSets().size() + " sets");
            msg = new StringBuilder();
            Map<String, EmbeddedSet> sortedSet = new TreeMap<>();
            for (EmbeddedSet set : v.getSets())
                sortedSet.put(set.getId(), set);
            // sets that existed before and still exist...
            int setsCount = 0;
            for (Entry<String, EmbeddedSet> entry : sortedSet.entrySet()) {
                if (result.getNamesMapping().containsKey(entry.getValue().getName())) {
                    msg.append("      >> ").append(entry.getValue().getId()).append(": [").append(entry.getValue().getTag()).append("] ").append(entry.getValue().getName()).append(" (");
                    msg.append(entry.getValue().getInclusions().size()).append(" edits");
                    String nameFrom = result.getNamesMapping().get(entry.getValue().getName()).getName();
                    if (!nameFrom.equals(entry.getValue().getName()))
                        msg.append("; mapped from '").append(nameFrom).append("'");
                    msg.append(")\n");
                    setsCount++;
                }
            }
            if (setsCount > 0) {
                msg.setLength(msg.length() - 1);
                _LOG.info("  > following " + setsCount + " sets already existed in previous version and still exist:");
                _LOG.info(msg.toString());
            }
            else
                _LOG.info("  > no set got retained");
            // sets that got added
            setsCount = 0;
            msg = new StringBuilder();
            for (Entry<String, EmbeddedSet> entry : sortedSet.entrySet()) {
                if (!result.getNamesMapping().containsKey(entry.getValue().getName())) {
                    msg.append("      >> ").append(entry.getValue().getId()).append(": [").append(entry.getValue().getTag()).append("] ").append(entry.getValue().getName()).append(" (");
                    msg.append(entry.getValue().getInclusions().size()).append(" edits)\n");
                    setsCount++;
                }
            }
            if (setsCount > 0) {
                msg.setLength(msg.length() - 1);
                _LOG.info("  > following " + setsCount + " sets got added:");
                _LOG.info(msg.toString());
            }
            else
                _LOG.info("  > no set got added");
        }
        // sets that got deleted
        Map<String, TranslationMapping> prevSetMappings = new TreeMap<>(previousInfo.getAllPreviousSetsMapping());
        for (TranslationMapping dto : result.getNamesMapping().values())
            prevSetMappings.remove(dto.getTag());
        if (prevSetMappings.size() > 0) {
            _LOG.info("  > following " + prevSetMappings.size() + " sets got removed:");
            msg = new StringBuilder();
            for (Entry<String, TranslationMapping> entry : prevSetMappings.entrySet())
                msg.append("      >> ").append(entry.getValue().getId()).append(": [").append(entry.getValue().getTag()).append("] ").append(entry.getValue().getName()).append("\n");
            msg.setLength(msg.length() - 1);
            _LOG.info(msg.toString());
        }
        else
            _LOG.info("  > no set got removed");

        if (conf.isDryMode()) {
            _LOG.info("  > dry mode on, skipping file creation...");
            return result;
        }

        // write the files
        File validatorFile = new File(outputDir, conf.getTranslationPrefix().toLowerCase() + "-translated-edits.xml");
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(validatorFile.toPath()), StandardCharsets.US_ASCII)) {
            ValidationXmlUtils.writeValidatorToXml(result.getValidator(), writer);
        }
        File gzFile = new File(outputDir, conf.getTranslationPrefix().toLowerCase() + "-translated-edits.xml.gz");
        try (GZIPOutputStream gzWriter = new GZIPOutputStream(Files.newOutputStream(gzFile.toPath()))) {
            ValidationXmlUtils.writeValidatorToXml(result.getValidator(), gzWriter);
        }

        // create the output that needs to become the input of the next translation...
        previousInfo.writeMappings(result.getNewEditsMappings(), new File(outputDir, conf.getEditMappingsFilename()));
        previousInfo.writeMappings(result.getNewSetsMappings(), new File(outputDir, conf.getSetMappingsFilename()));
        result.setNewValidatorProperties(ValidationXmlUtils.getXmlValidatorRootAttributes(validatorFile.toURI().toURL()));
        try (Writer propsWriter = new OutputStreamWriter(Files.newOutputStream(new File(outputDir, conf.getValidatorPropertiesFilename()).toPath()), StandardCharsets.US_ASCII)) {
            for (Entry<String, String> entry : new TreeMap<>(ValidationXmlUtils.getXmlValidatorRootAttributes(validatorFile.toURI().toURL())).entrySet())
                propsWriter.write(entry.getKey() + "=" + entry.getValue() + "\r\n"); // can't just write a Properties object bc Java writes the current date!
        }

        _LOG.info("Created " + validatorFile.getPath());

        // test the validator
        _LOG.info("\r\nTesting validator by loading it into the validation engine...");
        long start = System.currentTimeMillis();
        v = ValidationXmlUtils.loadValidatorFromXml(validatorFile);
        _LOG.info("  > successfully parsed XML file in " + SeerUtils.formatTime(System.currentTimeMillis() - start));
        try {
            start = System.currentTimeMillis();
            ValidationEngine.getInstance().addValidator(new EditableValidator(v));
            _LOG.info("  > successfully loaded validator '" + v.getId() + "' from file '" + validatorFile.getName() + "' in " + SeerUtils.formatTime(System.currentTimeMillis() - start));
        }
        catch (ConstructionException e) {
            throw new TranslationException("Unable to load new validator in validation engine", e);
        }

        // create Groovy source code
        if (conf.generateGroovySourceCode()) {
            _LOG.info("\r\nCreating Groovy source code...");
            conf.getRuntimeGenerator().createRuntimeFiles(v, conf);
        }

        if (!result.getTranslateErrorMessages().isEmpty()) {
            _LOG.info("\r\n\r\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            _LOG.info("Some errors were reported during the translation (they were logged earlier):\r\n" + String.join("\r\n", result.getTranslateErrorMessages()));
            _LOG.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        return result;
    }

    protected void validateConfiguration(TranslationConfiguration conf, boolean validateFilesAndDirs) throws TranslationException {
        if (conf == null)
            throw new TranslationException("Configuration is required");

        // metafile
        if (conf.getMetafileName() == null)
            throw new TranslationException("Metafile name is required");

        // translation prefix
        if (conf.getTranslationPrefix() == null)
            throw new TranslationException("Translation prefix is required");
        if (!conf.getTranslationPrefix().matches("[A-Z\\d_-]+"))
            throw new TranslationException("Translation prefix can only contain uppercase letters, digits, underscores and dashes");

        if (validateFilesAndDirs) {

            // working dir
            if (conf.getWorkingDirectoryPath() == null)
                throw new TranslationException("Working directory path is required");
            File workingDir = new File(conf.getWorkingDirectoryPath());
            if (!workingDir.exists())
                throw new TranslationException("Working directory path must point to an existing directory");

            // metafile
            File metafile = new File(workingDir, conf.getMetafileName());
            if (!metafile.exists())
                throw new TranslationException("Unable to find requested metafile");

            // previous working dir
            if (conf.getPreviousWorkingDirectoryPath() != null && !(new File(conf.getPreviousWorkingDirectoryPath()).exists()))
                throw new TranslationException("If provided, previous working directory path must point to an existing directory");

            // output dir/file names
            if (conf.getOutputDirectoryName() == null)
                throw new TranslationException("Output directory name is required");
            if (conf.getEditMappingsFilename() == null)
                throw new TranslationException("Edit mappings file name is required");
            if (conf.getSetMappingsFilename() == null)
                throw new TranslationException("Set mappings file name is required");
            if (conf.getValidatorPropertiesFilename() == null)
                throw new TranslationException("Validator properties file name is required");
        }

        // naaccr version
        if (conf.getNaaccrVersion() != null && !(NumberUtils.isDigits(conf.getNaaccrVersion()) && conf.getNaaccrVersion().length() == 3))
            throw new TranslationException("Invalid NAACCR version: " + conf.getNaaccrVersion());

        // ID generation
        if (conf.getIdGeneration() == null)
            throw new TranslationException("ID Generation is required");

        // resolvers
        if (conf.getMetafileResolver() == null)
            throw new TranslationException("Metafile Resolver is required");
        if (conf.getFieldResolver() == null)
            throw new TranslationException("Field Resolver is required");
        if (conf.getLogicResolver() == null)
            throw new TranslationException("Logic Resolver is required");

        // num compilation threads
        if (conf.getNumCompilationThreads() < 1 || conf.getNumCompilationThreads() > 10)
            throw new TranslationException("Number of compilation threads must be between 1 and 10");

        // source code generation
        if (conf.generateGroovySourceCode() && conf.getRuntimeGenerator() == null)
            throw new TranslationException("Runtime Generator is required when generating source code");
    }

    protected File createOrEmptyDir(File dir) throws TranslationException {
        if (!dir.exists()) {
            if (!dir.mkdir())
                throw new TranslationException("Unable to create " + dir.getPath());
        }
        else {
            try {
                SeerUtils.emptyDirectory(dir);
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to empty " + dir.getPath(), e);
            }
        }

        return dir;
    }

    protected String evaluateNewValidatorVersion(TranslationConfiguration conf, PreviousTranslationInfo previousInfo) throws TranslationException {
        String newVersion = conf.getTranslationVersion();
        if (newVersion == null)
            newVersion = previousInfo.computeVersion(conf);
        return newVersion;
    }

    public Validator translateMetafile(Metafile metafile, TranslationConfiguration conf) throws TranslationException {
        validateConfiguration(conf, false);

        return createValidator(metafile, conf, new PreviousTranslationInfo(conf)).getValidator();
    }

    protected TranslationResult createValidator(Metafile mf, TranslationConfiguration conf, PreviousTranslationInfo previousInfo) throws TranslationException {
        List<String> errors = new ArrayList<>();

        String mfPrefix = conf.getTranslationPrefix();
        String ruleIdPrefix = mfPrefix.toUpperCase() + "-";

        String today = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        Map<String, TranslationMapping> newEditsMappings = new HashMap<>();
        Map<String, TranslationMapping> newSetsMappings = new HashMap<>();

        // resolve the fields (setting the property ID to use)
        for (MetafileField field : mf.getFields())
            field.setPropertyName(conf.getFieldResolver().resolveField(field, conf));

        // **** HANDLE THE VALIDATOR ****
        Validator v = new Validator();
        v.setValidatorId(ValidationServices.getInstance().getNextValidatorSequence());
        v.setId(mfPrefix.toLowerCase() + "-translated");
        if (conf.getMinValidationEnginVersion() != null)
            v.setMinEngineVersion(conf.getMinValidationEnginVersion());
        v.setTranslatedFrom(conf.getMetafileName());
        v.setName(conf.getMetafileName().replace(".smf", "").replace(".SMF", ""));
        v.setVersion(evaluateNewValidatorVersion(conf, previousInfo));
        ValidatorVersion version = new ValidatorVersion(v.getVersion());
        ValidatorRelease release = new ValidatorRelease(version, new Date(), "Edits translated from " + conf.getMetafileName() + " on " + today);
        v.getReleases().add(release);

        // set current ID to the maximum contained in the original mappings
        int currentIdx = 0;
        for (TranslationMapping dto : previousInfo.getAllPreviousEditsMapping().values())
            currentIdx = Math.max(currentIdx, Integer.parseInt(StringUtils.split(dto.getId(), '-')[1]));
        currentIdx++;

        // **** HANDLE THE CATEGORIES ****
        Map<String, String> agencies = new HashMap<>();
        for (String agency : new TreeSet<>(mf.getAgencies().stream().map(MetafileAgency::getAdminCode).collect(Collectors.toList()))) {
            Category cat = new Category();
            cat.setCategoryId(ValidationServices.getInstance().getNextCategorySequence());
            cat.setId(mfPrefix.toLowerCase() + "-" + agency.toLowerCase() + "-edits");
            cat.setName("All " + agency.toUpperCase() + " Edits");
            cat.setValidator(v);
            v.getCategories().add(cat);
            agencies.put(agency, cat.getId());
        }

        // we are going to reference the tables by name a lot...
        Map<String, MetafileTable> tables = mf.getTables().stream().collect(Collectors.toMap(MetafileTable::getName, Function.identity()));

        // **** HANDLE THE RULES ****
        SortedMap<String, TranslationTable> usedTablesAndIndexes = new TreeMap<>();
        for (MetafileEdit edit : mf.getEdits()) {
            String tag = edit.getTag();
            String name = edit.getName();

            // translate the edit logic
            EditTranslationResult result = translateEdit(edit, tables, conf, true);
            if (result.getTranslationErrorMessage() != null)
                errors.add(" > unable to translate \"" + edit.getName() + "\" (the logic was copied into a failure file): " + result.getTranslationErrorMessage());

            // build rule
            Rule r = new Rule();
            r.setRuleId(ValidationServices.getInstance().getNextRuleSequence());
            if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.FROM_NAME)
                r.setId(name);
            else if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.FROM_TAG_WITH_PREFIX)
                r.setId(ruleIdPrefix + SeerUtils.pad(tag, 5, "0", true));
            else if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.FROM_TAG_WITHOUT_PREFIX)
                r.setId(tag);
            else if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.INCREMENTAL) {
                TranslationMapping prevEditDto = previousInfo.getPreviousMappingForEditTag(tag, name);
                if (prevEditDto != null)
                    r.setId(prevEditDto.getId());
                else
                    r.setId(ruleIdPrefix + SeerUtils.pad(String.valueOf(currentIdx++), 5, "0", true));
            }
            else
                throw new RuntimeException("Unsupported ID generation: " + conf.getIdGeneration());
            r.setName(name);
            r.setTag(tag);
            r.setJavaPath("untrimmedlines.untrimmedline");
            try {
                r.setExpression(result.getGroovy());
            }
            catch (ConstructionException e) {
                throw new RuntimeException("Unable to translate '" + name + "'", e);
            }
            String msg = edit.getDefaultMessage().getMessage();
            if ("This edit has no default error message".equals(msg))
                msg = ValidationEngine.NO_MESSAGE_DEFINED_MSG;
            r.setMessage(MetafileUtils.fixMessage(msg, edit, true));
            r.setAgency(edit.getAgency().getAdminCode().toUpperCase());
            StringBuilder doc = new StringBuilder();
            if (edit.getDescription() != null)
                doc.append(edit.getDescription());
            if (!StringUtils.isBlank(edit.getAdminNotes())) {
                if (doc.length() > 0)
                    doc.append("\n\n");
                doc.append("Admin Notes\n***********\n").append(edit.getAdminNotes());
            }
            r.setDescription(doc.toString());
            r.setCategory(agencies.get(edit.getAgency().getAdminCode()));

            v.getRules().add(r);

            try {
                newEditsMappings.put(tag, new TranslationMapping(tag, name, r.getId(), convertToHex(computeSha1(edit.getLogic()))));
            }
            catch (IOException e) {
                throw new TranslationException("Unable to compute hash code", e);
            }

            // merge the used tables/indexes information)
            result.getUsedTablesAndIndexes().forEach((tableName, table) -> {
                TranslationTable tableDto = usedTablesAndIndexes.computeIfAbsent(tableName, TranslationTable::new);
                table.getIndexes().forEach((indexName, index) -> {
                    TranslationTableIndex indexDto = tableDto.getIndexes().get(indexName);
                    if (indexDto == null)
                        tableDto.getIndexes().put(indexName, index);
                    else if (!indexDto.matches(index))
                        throw new RuntimeException(
                                "Got two def of same idx on " + tableName + ": " + indexDto.getColumns() + " for " + indexDto.getName() + " and " + index.getColumns() + " for " + index.getName());
                    tableDto.getTableVarColumns().addAll(table.getTableVarColumns());
                });
            });
        }

        // **** HANDLE THE CONTEXTS ****
        Set<ContextEntry> rawContextSet = new HashSet<>();
        // add tables and indexes
        for (TranslationTable tableUsage : usedTablesAndIndexes.values()) {
            String tableNameWithPrefix = mfPrefix + "_" + tableUsage.getName();
            // add table but only the used columns; a column is used if it is referenced by an index or a tablevar
            rawContextSet.add(createContextEntry(v, tableNameWithPrefix, CONTEXT_TYPE_TABLE, generateTableExpression(tables.get(tableUsage.getName()), tableUsage)));
            // add indexes for current table
            for (Map.Entry<String, TranslationTableIndex> entry : new TreeMap<>(tableUsage.getIndexes()).entrySet()) {
                TranslationTableIndex index = entry.getValue();
                rawContextSet.add(createContextEntry(v, mfPrefix + "_" + index.getName(), CONTEXT_TYPE_TABLE_INDEX_DEF, generateIndexExpression(tableNameWithPrefix, index.getColumns())));
            }
        }
        // add special constants
        _GENEDITS_CONSTANTS.forEach((constantName, expression) -> rawContextSet.add(createContextEntry(v, mfPrefix + "_" + constantName, CONTEXT_TYPE_JAVA, expression)));
        v.setRawContext(rawContextSet);

        // **** HANDLE THE SETS ****
        currentIdx = 0;
        for (TranslationMapping dto : previousInfo.getAllPreviousSetsMapping().values())
            currentIdx = Math.max(currentIdx, Integer.parseInt(StringUtils.split(dto.getId(), '-')[2]));
        currentIdx++;
        Map<String, TranslationMapping> namesMappings = new HashMap<>(); /// current name to old mapping
        for (MetafileSet mfSet : mf.getSets()) {
            String setTag = mfSet.getTag();
            String setName = mfSet.getName();

            EmbeddedSet set = new EmbeddedSet();
            set.setSetId(ValidationServices.getInstance().getNextSetSequence());
            if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.FROM_TAG_WITH_PREFIX)
                set.setId(mfPrefix.toUpperCase() + "-SET-" + SeerUtils.pad(setTag, 3, "0", true));
            else if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.FROM_TAG_WITHOUT_PREFIX)
                set.setId(setTag);
            else if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.FROM_NAME)
                set.setId(setName);
            else if (conf.getIdGeneration() == TranslationConfiguration.IdGeneration.INCREMENTAL) {
                TranslationMapping prevSet = previousInfo.getPreviousMappingForSetTag(setTag);
                if (prevSet != null) {
                    namesMappings.put(setName, prevSet);
                    set.setId(prevSet.getId());
                }
                else
                    set.setId(mfPrefix.toUpperCase() + "-SET-" + SeerUtils.pad(String.valueOf(currentIdx++), 3, "0", true));
            }
            else
                throw new RuntimeException("Unsupported ID generation: " + conf.getIdGeneration());
            set.setName(setName);
            set.setTag(setTag);
            set.setDescription(mfSet.getDescription());
            Set<String> toInclude = new HashSet<>();
            for (MetafileEdit edit : mf.getEdits())
                if (edit.getSets().contains(mfSet))
                    toInclude.add(newEditsMappings.get(edit.getTag()).getId());
            set.setInclusions(toInclude);

            v.getSets().add(set);

            newSetsMappings.put(setTag, new TranslationMapping(setTag, setName, set.getId(), ""));
        }

        // sanity check on rule messages
        for (Rule r : v.getRules()) {
            if (r.getMessage().contains("%F1") || r.getMessage().contains("%F2") || r.getMessage().equals("This edit has no default error message")) {
                _LOG.error("!!! Message was not properly fixed for " + r.getId() + ": " + r.getMessage());
                errors.add(" > message was not properly fixed (field names were not replaced) for \"" + r.getName() + "\"" + ": " + r.getMessage());
            }
        }

        TranslationResult result = new TranslationResult();
        result.setValidator(v);
        result.setNewEditsMappings(newEditsMappings);
        result.setNewSetsMappings(newSetsMappings);
        result.setUsedTablesAndIndexes(usedTablesAndIndexes);
        result.setNamesMapping(namesMappings);
        result.setTranslateErrorMessages(errors);

        return result;
    }

    protected String convertToHex(byte[] hash) {
        StringBuilder builder = new StringBuilder();

        for (byte b : hash) {
            int v = b & 0xff;

            if (v < 16)
                builder.append('0');

            builder.append(Integer.toHexString(v));
        }

        return builder.toString();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    protected byte[] computeSha1(String string) throws IOException {
        byte[] buffer = new byte[1024];

        try (DigestInputStream digester = new DigestInputStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.US_ASCII)), MessageDigest.getInstance("SHA"))) {

            // read the data through the digester, which updates the internal message digest.
            while (digester.read(buffer) != -1) {
                /* do nothing */
            }

            // finalize using the default digest method.
            return digester.getMessageDigest().digest();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find digest provider for SHA1!.", e);
        }
        /* do nothing */
    }

    protected ContextEntry createContextEntry(Validator v, String key, String type, String expression) {
        ContextEntry e = new ContextEntry();
        e.setContextEntryId(ValidationServices.getInstance().getNextContextEntrySequence());
        e.setValidator(v);
        e.setKey(key);
        e.setExpression(expression);
        e.setType(type);
        return e;
    }

    protected String generateTableExpression(MetafileTable mfTable, TranslationTable tableUsage) {

        // compute the used columns; a column is used if it's referenced in an index, or in a table var
        Set<String> usedHeaders = new HashSet<>();
        for (TranslationTableIndex index : tableUsage.getIndexes().values())
            usedHeaders.addAll(index.getColumns());
        usedHeaders.addAll(tableUsage.getTableVarColumns());
        if (usedHeaders.isEmpty())
            throw new RuntimeException("Got no used columns for " + mfTable.getName() + "; this should not happen!");

        // [['header1', 'header2'], ['val1', 'val2']]
        StringBuilder buf = new StringBuilder("[");

        // write headers
        buf.append("[");
        for (String s : mfTable.getHeaders())
            if (usedHeaders.contains(s))
                buf.append("'").append(s.replace("'", "\\\\'")).append("',");
        buf.setLength(buf.length() - 1);
        buf.append("],");

        // translate used headers into used columns index (value is the type of column to know if we have to quote it or not)
        Map<Integer, String> usedColumnIdx = new HashMap<>();
        for (String header : usedHeaders) {
            MetafileColumnMetaData metadata = mfTable.getMetaData() == null ? null : mfTable.getMetaData().get(header);
            usedColumnIdx.put(mfTable.getHeaders().indexOf(header), metadata != null && metadata.getColumnType() != null ? metadata.getColumnType() : "VARCHAR");
        }

        // write content
        for (int rowIdx = 0; rowIdx < mfTable.getContent().size(); rowIdx++) {
            List<String> row = mfTable.getContent().get(rowIdx);
            if (row.size() != mfTable.getHeaders().size())
                throw new RuntimeException(mfTable.getName() + " (row " + rowIdx + ") - was expecting " + mfTable.getHeaders().size() + " fields, got " + row.size());
            buf.append("[");
            for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                String colType = usedColumnIdx.get(colIdx);
                if (colType != null) {
                    String val = Objects.toString(row.get(colIdx), ""); // recent versions have started to include NULL values in their tables

                    if ("CHAR".equals(colType) || "VARCHAR".equals(colType))
                        buf.append("'").append(val.replace("'", "\\\\'")).append("'");
                    else
                        buf.append(val);

                    buf.append(",");
                }
            }
            buf.setLength(buf.length() - 1);
            buf.append("],");
        }
        buf.setLength(buf.length() - 1);
        buf.append("]");

        // some tables have non-ASCII characters in them, we are not going to support that!
        return buf.toString().replaceAll("\\p{Cntrl}", " ");
    }

    protected String generateIndexExpression(String tableName, List<String> columns) {
        // ['table': 'tableX', 'columns' : 'header1,header2']
        StringBuilder buf = new StringBuilder();
        buf.append("['table': '").append(tableName).append("', 'columns': '").append(StringUtils.join(columns, ',')).append("']");
        // some tables have non-ASCII characters in them, we are not going to support that!
        return buf.toString().replaceAll("\\p{Cntrl}", " ");
    }

    public EditTranslationResult translateEdit(MetafileEdit edit, Metafile metafile, TranslationConfiguration conf) throws TranslationException {

        // we have to "resolve" the fields, which is basically creating the property names from the field names (since Genedits uses field names in the logic)
        for (MetafileField field : metafile.getFields())
            field.setPropertyName(conf.getFieldResolver().resolveField(field, conf));

        return translateEdit(edit, metafile.getTables().stream().collect(Collectors.toMap(MetafileTable::getName, Function.identity())), conf, false);
    }

    protected EditTranslationResult translateEdit(MetafileEdit edit, Map<String, MetafileTable> tables, TranslationConfiguration conf, boolean createFailureFiles) {
        EditTranslationResult output = new EditTranslationResult();

        // reset some states (this is not a good design, but changing it would be a major undertaking)
        ParsedTree.resetState();

        try {
            // get the metafile logic
            String editLogic = conf.getLogicResolver().resolveLogic(edit, conf);

            // create the parser
            GeneditsParser parser = new GeneditsParser(new GeneditsLexer(new StringReader(editLogic)));

            // parse the logic (this will throw an exception if there is a syntax error in the logic)
            ParsedTree tree = (ParsedTree)parser.parse().value;

            // translate the parsed logic
            EditTranslationContext context = new EditTranslationContext(edit, conf.getTranslationPrefix(), tables);
            String translatedLogic = tree.translate(context);
            output.setGroovy(translatedLogic);
            output.setUsedTablesAndIndexes(context.getUsedTablesAndIndexes());
        }
        catch (Exception e) {
            _LOG.error("  > !!! Unable to parse '" + edit.getName() + "': " + e.getMessage());
            output.setTranslationErrorMessage(e.getMessage());
            output.setGroovy("// This edit could not be translated because it contains invalid syntax.\n\nreturn true");
            output.setUsedTablesAndIndexes(new HashMap<>());
            if (createFailureFiles) {
                try {
                    SeerUtils.writeFile("/** " + edit.getName() + " */\n\n" + edit.getLogic(), new File(conf.getWorkingDirectoryPath(), "failed-translation_" + edit.getTag() + ".txt"));
                }
                catch (IOException e2) {
                    throw new RuntimeException("Unable to write failed translation for edit " + edit.getName() + ": " + e2.getMessage());
                }
            }
        }

        return output;
    }
}
