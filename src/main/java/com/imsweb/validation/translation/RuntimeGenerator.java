/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.seerutils.SeerUtils;
import com.imsweb.validation.ValidationXmlUtils;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.Validator;
import com.imsweb.validation.runtime.RuntimeUtils;

public class RuntimeGenerator {

    protected static final Logger _LOG = LogManager.getLogger(RuntimeGenerator.class);

    protected static final Pattern _INNER_METHOD_PATTERN = Pattern.compile("^def [A-Za-z0-9_]+\\(\\) \\{$");

    public void createRuntimeFiles(Validator v, TranslationConfiguration conf) throws IOException, TranslationException {

        File rootSrcDir;
        if (conf.getGroovySourceCodeDirectoryPath() != null)
            rootSrcDir = new File(conf.getGroovySourceCodeDirectoryPath());
        else
            rootSrcDir = new File(conf.getWorkingDirectoryPath(), conf.getOutputDirectoryName());
        if (!rootSrcDir.exists() && !rootSrcDir.mkdirs())
            throw new IOException("Unable to create " + rootSrcDir.getPath());

        String packageName = conf.getGroovySourceCodePackage();
        if (StringUtils.isBlank(packageName))
            packageName = "com.imsweb.validation.edits.translated." + conf.getTranslationPrefix().toLowerCase();

        File runtimeSourceDir = new File(rootSrcDir, "src/main/groovy/" + packageName.replace(".", "/"));
        if (!runtimeSourceDir.exists() && !runtimeSourceDir.mkdirs())
            throw new IOException("Unable to create " + runtimeSourceDir.getPath());
        SeerUtils.emptyDirectory(runtimeSourceDir);

        // create logic sources files; some metafiles are too big to create one source file and have to be split (that's defined in the configuration)
        for (Pair<File, Integer> pair : createCompiledRules(v, runtimeSourceDir, packageName, conf.getGroovySourceCodeNumFiles()))
            _LOG.info("  > created " + pair.getLeft().getPath() + " [" + pair.getRight() + " edits]");

        // create parsed properties source file
        _LOG.info("  > created " + createParsedProperties(v, runtimeSourceDir, packageName).getPath());

        // create parsed lookups source file
        _LOG.info("  > created " + createParsedLookups(v, runtimeSourceDir, packageName).getPath());

        // create parsed context entries source file
        _LOG.info("  > created " + createParsedContexts(v, runtimeSourceDir, packageName).getPath());

        // create the validator XML file in resources
        File resourcesDir = new File(rootSrcDir, "src/main/resources/" + getResourcePath(conf));
        if (!resourcesDir.exists() && !resourcesDir.mkdirs())
            throw new IOException("Unable to create " + resourcesDir.getPath());
        SeerUtils.emptyDirectory(resourcesDir);
        File targetXmlFile = new File(resourcesDir, conf.getTranslationPrefix().toLowerCase() + "-translated-edits.xml");
        ValidationXmlUtils.writeValidatorToXml(v, targetXmlFile);
        _LOG.info("  > created " + targetXmlFile.getPath());

        // create "static" runtime file (this one doesn't change over time)
        _LOG.info("  > created " + createStaticRuntime(v, runtimeSourceDir, packageName, conf, targetXmlFile).getPath());
    }

    public List<Pair<File, Integer>> createCompiledRules(Validator v, File targetDir, String packageName, int numFiles) throws IOException, TranslationException {
        List<Pair<File, Integer>> groovyEditsFiles = new ArrayList<>();

        List<Rule> sortedRules = getSortedRules(v.getRules());

        // some metafiles are too big to create one source file...
        List<List<Rule>> splitLists = new ArrayList<>();
        if (numFiles > 1) {
            int size = sortedRules.size() / numFiles;
            for (int i = 0; i < numFiles; i++)
                splitLists.add(sortedRules.subList(i * size, i == numFiles - 1 ? sortedRules.size() : i * size + size));
        }
        else
            splitLists = Collections.singletonList(sortedRules);

        int counter = 1;
        for (List<Rule> rules : splitLists) {
            String className = RuntimeUtils.createCompiledRulesClassName(v.getId()) + (splitLists.size() > 1 ? counter++ : "");

            File groovyEditsFile = new File(targetDir, className + ".groovy");

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(groovyEditsFile), StandardCharsets.UTF_8))) {
                writer.write("package " + packageName + "\r\n");
                writer.write("\r\n");
                writer.write("import com.imsweb.validation.functions.MetafileContextFunctions\r\n");
                writer.write("import com.imsweb.validation.runtime.CompiledRules\r\n");
                writer.write("import groovy.transform.CompileStatic\r\n");
                writer.write("\r\n");
                writer.write("@CompileStatic\r\n");
                writer.write("class " + className + " implements CompiledRules {\r\n");
                writer.write("\r\n");
                writer.write("    @Override\r\n");
                writer.write("    public String getValidatorId() {\r\n");
                writer.write("        return '" + v.getId() + "'\r\n");
                writer.write("    }\r\n");
                writer.write("\r\n");
                writer.write("    @Override\r\n");
                writer.write("    public String getValidatorVersion() {\r\n");
                writer.write("        return '" + v.getVersion() + "'\r\n");
                writer.write("    }\r\n");
                writer.write("\r\n");
                writer.write("    @Override\r\n");
                writer.write("    public Map<String, List<Class<?>>> getMethodParameters() {\r\n");
                writer.write("        return [\r\n");
                writer.write("            'untrimmedlines' : [Binding.class, Map.class, MetafileContextFunctions.class, List.class],\r\n");
                writer.write("            'untrimmedlines.untrimmedline' : [Binding.class, Map.class, MetafileContextFunctions.class, List.class, Map.class]\r\n");
                writer.write("        ]\r\n");
                writer.write("    }\r\n");
                writer.write("\r\n");
                if (splitLists.size() > 1) {
                    writer.write("    @Override\r\n");
                    writer.write("    public boolean containsRuleId(String id) {\r\n");
                    writer.write("        return '" + rules.get(0).getId() + "' <= id && id <= '" + rules.get(rules.size() - 1).getId() + "'\r\n");
                    writer.write("    }\r\n");
                    writer.write("\r\n");
                }
                for (Rule r : rules) {
                    List<String> lines = Arrays.asList(r.getExpression().split("\r?\n"));
                    if (!hasInnerMethod(lines)) {
                        writer.write("    // ID: " + r.getId() + "; TAG: " + (r.getTag() == null ? "<no tag>" : r.getTag()) + "; NAME: " + r.getName() + "\r\n");
                        writer.write("    public boolean " + RuntimeUtils.createMethodName(r.getId()));
                        writer.write("(Binding binding, Map<String, Object> context, MetafileContextFunctions functions, List<Map<String, String>> untrimmedlines, Map<String, String> untrimmedline)");
                        writer.write(" throws Exception {\r\n");
                        for (String line : lines)
                            writer.write("        " + line.replace("Functions.", "functions.").replace("Context.", "context.") + "\r\n");
                        writer.write("\r\n");
                        writer.write("    }\r\n");
                        writer.write("\r\n");
                    }
                    else
                        throw new TranslationException("can't pre-compile " + r.getId() + " because it has an inner function...");
                }
                writer.write("}\r\n");
            }

            groovyEditsFiles.add(Pair.of(groovyEditsFile, rules.size()));
        }

        return groovyEditsFiles;
    }

    public File createParsedProperties(Validator v, File targetDir, String packageName) throws IOException {
        File groovyPropertiesFile = new File(targetDir, RuntimeUtils.createParsedPropertiesClassName(v.getId()) + ".groovy");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(groovyPropertiesFile), StandardCharsets.UTF_8))) {
            writer.write("package " + packageName + "\r\n");
            writer.write("\r\n");
            writer.write("import com.imsweb.validation.runtime.ParsedProperties\r\n");
            writer.write("import groovy.transform.CompileStatic\r\n");
            writer.write("\r\n");
            writer.write("@CompileStatic\r\n");
            writer.write("class " + RuntimeUtils.createParsedPropertiesClassName(v.getId()) + " implements ParsedProperties {\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public String getValidatorId() {\r\n");
            writer.write("        return '" + v.getId() + "'\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public String getValidatorVersion() {\r\n");
            writer.write("        return '" + v.getVersion() + "'\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            for (Rule r : getSortedRules(v.getRules())) {
                List<String> lines = Arrays.asList(r.getExpression().split("\r?\n"));
                if (!hasInnerMethod(lines)) {
                    writer.write("    // ID: " + r.getId() + "; TAG: " + (r.getTag() == null ? "<no tag>" : r.getTag()) + "; NAME: " + r.getName() + "\r\n");
                    writer.write("    public Set<String> " + RuntimeUtils.createMethodName(r.getId()) + "() {\r\n");
                    if (r.getUsedProperties().isEmpty())
                        writer.write("        return new HashSet()\r\n");
                    else {
                        writer.write("        Set<String> s = new HashSet<String>()\r\n");
                        for (String s : r.getUsedProperties())
                            writer.write("        s.add('" + s + "')\r\n");
                        writer.write("        return s\r\n");
                    }
                    writer.write("    }\r\n");
                    writer.write("\r\n");
                }
            }
            writer.write("}\r\n");
        }

        return groovyPropertiesFile;
    }

    public File createParsedLookups(Validator v, File targetDir, String packageName) throws IOException {
        File groovyLookupsFile = new File(targetDir, RuntimeUtils.createParsedLookupsClassName(v.getId()) + ".groovy");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(groovyLookupsFile), StandardCharsets.UTF_8))) {
            writer.write("package " + packageName + "\r\n");
            writer.write("\r\n");
            writer.write("import com.imsweb.validation.runtime.ParsedLookups\r\n");
            writer.write("import groovy.transform.CompileStatic\r\n");
            writer.write("\r\n");
            writer.write("@CompileStatic\r\n");
            writer.write("class " + RuntimeUtils.createParsedLookupsClassName(v.getId()) + " implements ParsedLookups {\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public String getValidatorId() {\r\n");
            writer.write("        return '" + v.getId() + "'\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public String getValidatorVersion() {\r\n");
            writer.write("        return '" + v.getVersion() + "'\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            for (Rule r : getSortedRules(v.getRules())) {
                List<String> lines = Arrays.asList(r.getExpression().split("\r?\n"));
                if (!hasInnerMethod(lines)) {
                    writer.write("    // ID: " + r.getId() + "; TAG: " + (r.getTag() == null ? "<no tag>" : r.getTag()) + "; NAME: " + r.getName() + "\r\n");
                    writer.write("    public Set<String> " + RuntimeUtils.createMethodName(r.getId()) + "() {\r\n");
                    if (r.getUsedLookupIds().isEmpty())
                        writer.write("        return new HashSet()\r\n");
                    else {
                        writer.write("        Set<String> s = new HashSet<String>()\r\n");
                        for (String s : r.getUsedLookupIds())
                            writer.write("        s.add('" + s + "')\r\n");
                        writer.write("        return s\r\n");
                    }
                    writer.write("    }\r\n");
                    writer.write("\r\n");
                }
            }
            writer.write("}\r\n");
        }

        return groovyLookupsFile;
    }

    public File createParsedContexts(Validator v, File targetDir, String packageName) throws IOException {
        File groovyContextsFile = new File(targetDir, RuntimeUtils.createParsedContextsClassName(v.getId()) + ".groovy");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(groovyContextsFile), StandardCharsets.UTF_8))) {
            writer.write("package " + packageName + "\r\n");
            writer.write("\r\n");
            writer.write("import com.imsweb.validation.runtime.ParsedContexts\r\n");
            writer.write("import groovy.transform.CompileStatic\r\n");
            writer.write("\r\n");
            writer.write("@CompileStatic\r\n");
            writer.write("class " + RuntimeUtils.createParsedContextsClassName(v.getId()) + " implements ParsedContexts {\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public String getValidatorId() {\r\n");
            writer.write("        return '" + v.getId() + "'\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public String getValidatorVersion() {\r\n");
            writer.write("        return '" + v.getVersion() + "'\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            for (Rule r : getSortedRules(v.getRules())) {
                List<String> lines = Arrays.asList(r.getExpression().split("\r?\n"));
                if (!hasInnerMethod(lines)) {
                    writer.write("    // ID: " + r.getId() + "; TAG: " + (r.getTag() == null ? "<no tag>" : r.getTag()) + "; NAME: " + r.getName() + "\r\n");
                    writer.write("    public Set<String> " + RuntimeUtils.createMethodName(r.getId()) + "() {\r\n");
                    Set<String> contexts = new HashSet<>(r.getUsedContextKeys());
                    contexts.remove("binding"); // obvious false positive, let's remove it...
                    if (contexts.isEmpty())
                        writer.write("        return new HashSet()\r\n");
                    else {
                        writer.write("        Set<String> s = new HashSet<String>()\r\n");
                        for (String s : contexts)
                            writer.write("        s.add('" + s + "')\r\n");
                        writer.write("        return s\r\n");
                    }
                    writer.write("    }\r\n");
                    writer.write("\r\n");
                }
            }
            writer.write("}\r\n");
        }

        return groovyContextsFile;
    }

    public File createStaticRuntime(Validator v, File targetDir, String packageName, TranslationConfiguration conf, File xmlFile) throws IOException {

        StringBuilder className = new StringBuilder();
        for (String s : StringUtils.split(v.getId(), "-"))
            className.append(StringUtils.capitalize(s));
        className.append("RuntimeEdits");

        File groovyRuntimeFile = new File(targetDir, className + ".groovy");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(groovyRuntimeFile), StandardCharsets.UTF_8))) {
            writer.write("package " + packageName + "\r\n");
            writer.write("\r\n");
            writer.write("import com.imsweb.validation.ValidationXmlUtils\r\n");
            writer.write("import com.imsweb.validation.entities.Validator\r\n");
            writer.write("import com.imsweb.validation.runtime.*\r\n");
            writer.write("import groovy.transform.CompileStatic\r\n");
            writer.write("\r\n");
            writer.write("@CompileStatic\r\n");
            writer.write("class " + className + " implements RuntimeEdits {\r\n");
            writer.write("\r\n");
            writer.write("    public static Validator loadValidator() {\r\n");
            writer.write("        try {\r\n");
            writer.write("            return ValidationXmlUtils.loadValidatorFromXml(getXmlUrl(), new " + className + "())\r\n");
            writer.write("        }\r\n");
            writer.write("        catch (IOException e) {\r\n");
            writer.write("            throw new RuntimeException(\"Unable to load validator\", e)\r\n");
            writer.write("        }\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    public static URL getXmlUrl() {\r\n");
            writer.write("        return Thread.currentThread().getContextClassLoader().getResource(\"" + getResourcePath(conf) + "/" + xmlFile.getName() + "\")\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public CompiledRules getCompiledRules() {\r\n");
            if (conf.getGroovySourceCodeNumFiles() == 1)
                writer.write("        return new " + RuntimeUtils.createCompiledRulesClassName(v.getId()) + "()\r\n");
            else {
                writer.write("        return new CompiledRulesBundle(\r\n");
                for (int i = 1; i <= conf.getGroovySourceCodeNumFiles(); i++) {
                    writer.write("                new " + RuntimeUtils.createCompiledRulesClassName(v.getId()) + i + "()");
                    if (i < conf.getGroovySourceCodeNumFiles())
                        writer.write(",\r\n");
                    else
                        writer.write(")\r\n");
                }
            }
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public ParsedProperties getParsedProperties() {\r\n");
            writer.write("        return new " + RuntimeUtils.createParsedPropertiesClassName(v.getId()) + "()\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public ParsedContexts getParsedContexts() {\r\n");
            writer.write("        return new " + RuntimeUtils.createParsedContextsClassName(v.getId()) + "()\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("    @Override\r\n");
            writer.write("    public ParsedLookups getParsedLookups() {\r\n");
            writer.write("        return new " + RuntimeUtils.createParsedLookupsClassName(v.getId()) + "()\r\n");
            writer.write("    }\r\n");
            writer.write("\r\n");
            writer.write("}\r\n");
        }

        return groovyRuntimeFile;
    }

    protected String getResourcePath(TranslationConfiguration conf) {
        String path = conf.getXmlFileSourcePath();
        if (StringUtils.isBlank(path))
            path = "edits/translated/" + conf.getTranslationPrefix().toLowerCase();
        return path;
    }

    // can't pre-compile edits that have inner methods (yet)
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean hasInnerMethod(List<String> lines) {
        for (String line : lines)
            if (_INNER_METHOD_PATTERN.matcher(line).matches())
                return true;
        return false;
    }

    // helper
    protected List<Rule> getSortedRules(Set<Rule> rules) {
        List<Rule> list = new ArrayList<>(rules);
        list.sort(Comparator.comparing(Rule::getId));
        return list;
    }

}
