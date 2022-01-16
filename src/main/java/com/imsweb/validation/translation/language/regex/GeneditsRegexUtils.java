/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.regex;

import java.io.StringReader;
import java.util.regex.Pattern;

public final class GeneditsRegexUtils {

    private static final Pattern _PATTERN_ALL_SPACES = Pattern.compile("\\s+");

    private GeneditsRegexUtils() {
    }

    public static String translateRegex(String regex) {
        String result;

        // special case, if all spaces, then translate that to an "all-spaces" java regex (the logic after this right-trims the value)
        if (_PATTERN_ALL_SPACES.matcher(regex).matches())
            result = "\\s{" + regex.length() + "}";
        else {
            try {
                // looks like they ignore spaces after commas, and at the end of the regex...
                result = (String)(new GeneditsRegexParser(new GeneditsRegexLexer(new StringReader(regex.replaceAll(",\\s+", ",").replaceAll("\\s+$", "")))).parse().value);
            }
            catch (Exception e) {
                throw new RuntimeException("Unable to translate regex '" + regex + "'", e);
            }
        }

        if (result == null)
            throw new RuntimeException("Unable to translate regex '" + regex + "'");

        return result;
    }

}
