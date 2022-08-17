/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.util.List;

import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;

@SuppressWarnings("unused")
public class TranslationConfiguration {

    // ID generation is used for edit and set IDs
    enum IdGeneration {

        // the edit/set IDs will be set to the next available number within the validator with the validator ID as a prefix;
        // with this option, previous IDs will be retained for edits with the same tag if that information is provided to the translation
        INCREMENTAL,

        // the edit/set IDs will be set to their metafile tag with the validator ID as a prefix
        FROM_TAG_WITH_PREFIX,

        // the edit/set IDs will be set to their metafile tag without the validator ID as prefix
        FROM_TAG_WITHOUT_PREFIX,

        // the edit/set IDS will be set to their metafile name
        FROM_NAME
    }

    // the metafile name (required)
    private String _metafileName;

    // the path to the directory containing the metafile to be translated (required)
    private String _workingDirectoryPath;

    // the path to the dictionary containing the previous translation; its output will be used as input of the new translation (optional)
    private String _previousWorkingDirectoryPath;

    // the translation prefix (required) - example of prefixes: NAACCR, SEER, NPRC (a lot of created resources will use that prefix)
    private String _translationPrefix;

    // the translation version (optional) - if not provided, the "previous" version will be used and incremented, if no previous translation is available, "1.0" will be used
    private String _translationVersion;

    // set to true to increment the minor part of the version (by default the major one is incremented); defaults to false, ignored if a full version is provided
    private boolean _isMinorVersion;

    // the NAACCR version of the metafile, this is used by the translation to lookup field names (optional, if not provided, official NAACCR fields won't be used)
    private String _naaccrVersion;

    // the minimum version of the engine that can be used with the translated edits (optional)
    private String _minValidationEnginVersion;

    // the ID generation method (required, default to INCREMENTAL)
    private IdGeneration _idGeneration;

    // the metafile resolver (required but set to a default value in this class)
    private MetafileResolver _metafileResolver;

    // the field resolver used to derive property names from field names (required but set to a default value in this class)
    private FieldResolver _fieldResolver;

    // the logic resolver, can be used to tweak input logic on-the-fly to fix a syntax issue for example (required but set to a default value in this class)
    private LogicResolver _logicResolver;

    // the runtime generator (required only if source code is generated, set to a default value in this class)
    private RuntimeGenerator _runtimeGenerator;

    // if provided, the dictionaries will be available to the field resolver (and the default field resolver will use them).
    private List<NaaccrDictionary> _userDefinedDictionaries;

    // the number of threads to use for compiling the edits (defaults to 2)
    private int _numCompilationThreads;

    // whether the source code should be generated
    private boolean _generateGroovySourceCode;

    // the path to the directory to use for creating the source code (defaults to the output directory)
    private String _groovySourceCodeDirectoryPath;

    // the package to use for creating the source code (defaults to com.imsweb.validation.edits.translated.xxx where xxx is the translation prefix)
    private String _groovySourceCodePackage;

    // the number of source file to create (defaults to 1) - this only needs to be changed when translating big metafiles
    private int _groovySourceCodeNumFiles;

    // the path to the XML file in the resources of the source code (defaults to edits/translated/xxx where xxx is the translation prefix)
    private String _xmlFileSourcePath;

    // the name of the created output directory (defaults to "output")
    private String _outputDirectoryName;

    // the name of the created edit mappings file (defaults to "mappings-edits.txt")
    private String _editMappingsFilename;

    // the name of the created set mappings file (defaults to "mappings-sets.txt")
    private String _setMappingsFilename;

    // the name of the created validator properties file (defaults to "validator.properties")
    private String _validatorPropertiesFilename;

    /**
     * Constructor.
     */
    public TranslationConfiguration() {

        _outputDirectoryName = "output";
        _editMappingsFilename = "mappings-edits.txt";
        _setMappingsFilename = "mappings-sets.txt";
        _validatorPropertiesFilename = "validator.properties";

        _isMinorVersion = false;

        _idGeneration = IdGeneration.INCREMENTAL;

        _metafileResolver = new MetafileResolver();
        _fieldResolver = new FieldResolver();
        _logicResolver = new LogicResolver();
        _runtimeGenerator = new RuntimeGenerator();

        _numCompilationThreads = 2;

        _generateGroovySourceCode = false;
        _groovySourceCodeNumFiles = 1;
    }

    public String getMetafileName() {
        return _metafileName;
    }

    public void setMetafileName(String metafileName) {
        _metafileName = metafileName;
    }

    public String getWorkingDirectoryPath() {
        return _workingDirectoryPath;
    }

    public void setWorkingDirectoryPath(String workingDirectoryPath) {
        _workingDirectoryPath = workingDirectoryPath;
    }

    public String getPreviousWorkingDirectoryPath() {
        return _previousWorkingDirectoryPath;
    }

    public void setPreviousWorkingDirectoryPath(String previousWorkingDirectoryPath) {
        _previousWorkingDirectoryPath = previousWorkingDirectoryPath;
    }

    public String getOutputDirectoryName() {
        return _outputDirectoryName;
    }

    public void setOutputDirectoryName(String outputDirectoryName) {
        _outputDirectoryName = outputDirectoryName;
    }

    public String getEditMappingsFilename() {
        return _editMappingsFilename;
    }

    public void setEditMappingsFilename(String editMappingsFilename) {
        _editMappingsFilename = editMappingsFilename;
    }

    public String getSetMappingsFilename() {
        return _setMappingsFilename;
    }

    public void setSetMappingsFilename(String setMappingsFilename) {
        _setMappingsFilename = setMappingsFilename;
    }

    public String getValidatorPropertiesFilename() {
        return _validatorPropertiesFilename;
    }

    public void setValidatorPropertiesFilename(String validatorPropertiesFilename) {
        _validatorPropertiesFilename = validatorPropertiesFilename;
    }

    public String getTranslationPrefix() {
        return _translationPrefix;
    }

    public void setTranslationPrefix(String translationPrefix) {
        _translationPrefix = translationPrefix;
    }

    public String getNaaccrVersion() {
        return _naaccrVersion;
    }

    public void setNaaccrVersion(String naaccrVersion) {
        _naaccrVersion = naaccrVersion;
    }

    public String getTranslationVersion() {
        return _translationVersion;
    }

    public void setTranslationVersion(String translationVersion) {
        _translationVersion = translationVersion;
    }

    public boolean isMinorVersion() {
        return _isMinorVersion;
    }

    public void setMinorVersion(boolean minorVersion) {
        _isMinorVersion = minorVersion;
    }

    public String getMinValidationEnginVersion() {
        return _minValidationEnginVersion;
    }

    public void setMinValidationEnginVersion(String minValidationEnginVersion) {
        _minValidationEnginVersion = minValidationEnginVersion;
    }

    public IdGeneration getIdGeneration() {
        return _idGeneration;
    }

    public void setIdGeneration(IdGeneration idGeneration) {
        _idGeneration = idGeneration;
    }

    public MetafileResolver getMetafileResolver() {
        return _metafileResolver;
    }

    public void setMetafileResolver(MetafileResolver metafileResolver) {
        _metafileResolver = metafileResolver;
    }

    public FieldResolver getFieldResolver() {
        return _fieldResolver;
    }

    public void setFieldResolver(FieldResolver fieldResolver) {
        _fieldResolver = fieldResolver;
    }

    public LogicResolver getLogicResolver() {
        return _logicResolver;
    }

    public void setLogicResolver(LogicResolver logicResolver) {
        _logicResolver = logicResolver;
    }

    public RuntimeGenerator getRuntimeGenerator() {
        return _runtimeGenerator;
    }

    public void setRuntimeGenerator(RuntimeGenerator runtimeGenerator) {
        _runtimeGenerator = runtimeGenerator;
    }

    public List<NaaccrDictionary> getUserDefinedDictionaries() {
        return _userDefinedDictionaries;
    }

    public void setUserDefinedDictionaries(List<NaaccrDictionary> userDefinedDictionaries) {
        _userDefinedDictionaries = userDefinedDictionaries;
    }

    public boolean isGenerateGroovySourceCode() {
        return _generateGroovySourceCode;
    }

    public int getNumCompilationThreads() {
        return _numCompilationThreads;
    }

    public void setNumCompilationThreads(int numCompilationThreads) {
        _numCompilationThreads = numCompilationThreads;
    }

    public boolean generateGroovySourceCode() {
        return _generateGroovySourceCode;
    }

    public void setGenerateGroovySourceCode(boolean generateGroovySourceCode) {
        _generateGroovySourceCode = generateGroovySourceCode;
    }

    public String getGroovySourceCodeDirectoryPath() {
        return _groovySourceCodeDirectoryPath;
    }

    public void setGroovySourceCodeDirectoryPath(String groovySourceCodeDirectoryPath) {
        _groovySourceCodeDirectoryPath = groovySourceCodeDirectoryPath;
    }

    public String getGroovySourceCodePackage() {
        return _groovySourceCodePackage;
    }

    public void setGroovySourceCodePackage(String groovySourceCodePackage) {
        _groovySourceCodePackage = groovySourceCodePackage;
    }

    public int getGroovySourceCodeNumFiles() {
        return _groovySourceCodeNumFiles;
    }

    public void setGroovySourceCodeNumFiles(int groovySourceCodeNumFiles) {
        _groovySourceCodeNumFiles = groovySourceCodeNumFiles;
    }

    public String getXmlFileSourcePath() {
        return _xmlFileSourcePath;
    }

    public void setXmlFileSourcePath(String xmlFileSourcePath) {
        _xmlFileSourcePath = xmlFileSourcePath;
    }
}
