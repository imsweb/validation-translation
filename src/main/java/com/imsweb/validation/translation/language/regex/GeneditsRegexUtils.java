/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.regex;

import java.io.StringReader;

public final class GeneditsRegexUtils {

    private GeneditsRegexUtils() {
    }

    public static String translateRegex(String regex) {
        String result;
        try {
            // looks like they ignore spaces after commas, and at the end of the regex...
            result = (String)(new GeneditsRegexParser(new GeneditsRegexLexer(new StringReader(regex.replaceAll(",\\s+", ",").replaceAll("\\s+$", "")))).parse().value);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to translate regex '" + regex + "'", e);
        }

        if (result == null)
            throw new RuntimeException("Unable to translate regex '" + regex + "'");

        return result;
    }

}
