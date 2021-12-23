/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

@SuppressWarnings("unused")
public class TranslationConfiguration {

    enum IdGeneration {
        // the edit/set IDs will be set to the next available number within the validator with the validator ID as a prefix;
        // with this option, previous IDs will be retained for edits with the same tag if that information is provided to the translation
        INCREMENTAL,
        // the edit/set IDs will be set to their metafile tag with the validator ID as a prefix
        FROM_TAG,
        // the edit/set IDS will be set to their metafile name
        FROM_NAME
    }

    private String _metafileName;

    private String _workingDirectoryPath;

    private String _previousWorkingDirectoryPath;

    private String _outputDirectoryName;

    private String _editMappingsFilename;

    private String _setMappingsFilename;

    private String _validatorPropertiesFilename;

    private String _translationPrefix;

    private String _translationVersion;

    private boolean _isMinorVersion;

    private String _naaccrVersion;

    private String _minValidationEnginVersion;

    private IdGeneration _idGeneration;

    private MetafileResolver _metafileResolver;

    private FieldResolver _fieldResolver;

    private LogicResolver _logicResolver;

    private RuntimeGenerator _runtimeGenerator;

    private int _numCompilationThreads;

    private boolean _generateGroovySourceCode;

    private String _groovySourceCodeDirectoryPath;

    private String _groovySourceCodePackage;

    private int _groovySourceCodeNumFiles;

    private String _xmlFileSourcePath;

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
