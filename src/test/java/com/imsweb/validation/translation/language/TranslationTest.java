/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import java_cup.runtime.Symbol;

import com.imsweb.staging.Staging;
import com.imsweb.staging.cs.CsDataProvider;
import com.imsweb.staging.cs.CsDataProvider.CsVersion;
import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidationContextFunctions;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidationException;
import com.imsweb.validation.ValidationServices;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validatable;
import com.imsweb.validation.functions.MetafileContextFunctions;
import com.imsweb.validation.translation.EditTranslationContext;
import com.imsweb.validation.translation.language.entity.ParsedTree;
import com.imsweb.validation.translation.metafile.MetafileEdit;
import com.imsweb.validation.translation.metafile.MetafileField;

import static org.junit.Assert.fail;

public class TranslationTest {

    @BeforeClass
    public static void setup() {
        ValidationServices.initialize(new ValidationServices());
        ValidationContextFunctions.initialize(new MetafileContextFunctions(Staging.getInstance(CsDataProvider.getInstance(CsVersion.LATEST)), null, null));
        ValidationEngine.getInstance().initialize();
    }

    @Test
    public void testExtraBlankLines() {
        assertTranslations("extra-blank-lines");
    }

    @Test
    public void testBooleanVariables() {
        assertTranslations("boolean-variables");
    }

    @Test
    public void testInnerFunctions() {
        assertTranslations("inner-functions");
    }

    @Test
    public void testExtraBraces() {
        assertTranslations("extra-braces");
    }

    @Test
    public void testForbiddenKeywords() {
        assertTranslations("forbidden-keywords");
    }

    @Test
    public void testBooleanAndOrMix() {
        assertTranslations("boolean-and-or-mix");
    }

    @Test
    public void testBooleanCallComparedToInt() {
        assertTranslations("boolean-call-compared-to-int");
    }

    private Path getTestsDir() {
        return Paths.get(System.getProperty("user.dir") + "/src/test/resources/tests");
    }

    private void assertTranslations(String filenamePrefix) {
        try {
            for (String filename : Files.list(getTestsDir()).map(Path::getFileName).map(Path::toString).filter(f -> f.startsWith(filenamePrefix) && !f.contains("expected")).collect(
                    Collectors.toList()))
                assertTranslation(filename);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertTranslation(String filename) {

        // check the translation
        String originalLogic = readFile(filename);
        String translatedLogic = translate(originalLogic, filename);
        String expectedLogic = readFile(filename.replace(".txt", "-expected.txt"));
        if (!translatedLogic.equals(expectedLogic))
            fail("For file '" + filename + "'; expected\n>>>>>>>>>>\n" + expectedLogic + "\n>>>>>>>>>>\nbut got\n>>>>>>>>>>\n" + translatedLogic + "\n>>>>>>>>>>\n");

        // check if we have to actually run the edit
        try (BufferedReader br = Files.newBufferedReader(getTestsDir().resolve(filename))) {
            String expectedResultLine = br.lines().filter(l -> l.startsWith("#") && l.contains("@EXPECTED_RESULT@")).findFirst().orElse(null);
            if (expectedResultLine != null) {
                Rule rule = new Rule();
                rule.setId(filename);
                rule.setJavaPath("untrimmedlines.untrimmedline");
                rule.setExpression(expectedLogic);
                Validatable validatable = new SimpleNaaccrLinesValidatable(Collections.singletonList(new HashMap<>()), null, true);
                RuleFailure failure = ValidationEngine.getInstance().validate(validatable, rule).stream().findFirst().orElse(null);
                if (expectedResultLine.contains("PASS") && failure != null) {
                    String msg = ValidationEngine.EXCEPTION_MSG.equals(failure.getMessage()) ? failure.getGroovyException().getMessage() : failure.getMessage();
                    fail("Was expecting PASS for \"" + filename + "\" but got FAIL: " + msg);
                }
                if (!expectedResultLine.contains("PASS") && failure == null)
                    fail("Was expecting FAIL for \"" + filename + "\" but got PASS");
            }
        }
        catch (IOException | ConstructionException | ValidationException e) {
            throw new RuntimeException("Can't test " + filename, e);
        }
    }

    private String readFile(String file) {
        try (BufferedReader br = Files.newBufferedReader(getTestsDir().resolve(file))) {
            return br.lines().filter(l -> !l.startsWith("#")).collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            throw new RuntimeException("Can't read " + file, e);
        }
    }

    private String translate(String logic, String filename) {

        MetafileEdit edit = new MetafileEdit();
        edit.setName("TEST");

        MetafileField field1 = new MetafileField();
        field1.setName("FIELD1");
        field1.setNumber(10001);
        field1.setLength(4);
        field1.setPropertyName("field1");
        edit.setFields(Collections.singletonMap(1, field1));

        ParsedTree.resetState();

        GeneditsParser parser = new GeneditsParser(new GeneditsLexer(new StringReader(logic)));

        Symbol parsingResult;
        try {
            parsingResult = parser.parse();
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to parse '" + filename + "'", e);
        }
        if (parsingResult == null)
            throw new RuntimeException("Unable to parse '" + filename + "': bad syntax...");

        try {
            return ((ParsedTree)parsingResult.value).translate(new EditTranslationContext(edit, "TEST", null));
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to parse '" + filename + "'", e);
        }
    }
}
