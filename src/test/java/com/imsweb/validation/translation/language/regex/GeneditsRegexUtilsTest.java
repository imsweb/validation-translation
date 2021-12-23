/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.regex;

import org.junit.Assert;
import org.junit.Test;

public class GeneditsRegexUtilsTest {

    @Test
    public void testTranslateRegex() {
        Assert.assertEquals("(\\d\\d)", GeneditsRegexUtils.translateRegex("dd"));
        Assert.assertEquals("(\\d\\dff)", GeneditsRegexUtils.translateRegex("ddff"));
        Assert.assertEquals("(404 \\d\\d\\d\\-\\d\\d\\d\\d)", GeneditsRegexUtils.translateRegex("404 ddd!-dddd"));
        Assert.assertEquals("([7-9]\\d\\d\\d\\d\\d)", GeneditsRegexUtils.translateRegex("7:9ddddd"));
        Assert.assertEquals("([7-9]\\d\\d\\d\\d\\d)", GeneditsRegexUtils.translateRegex("7:9ddddd"));
        Assert.assertEquals("([A-Za-z]((.))*)", GeneditsRegexUtils.translateRegex("a{?}*"));
        Assert.assertEquals("([A-Za-z](([A-Za-z])|(\\s)|(\\-)|('))*)", GeneditsRegexUtils.translateRegex("a{a,b,-,'}*"));
        Assert.assertEquals("(I(\\+))", GeneditsRegexUtils.translateRegex("I{+}"));
        Assert.assertEquals("(I(\\+))", GeneditsRegexUtils.translateRegex("I[+]"));
        Assert.assertEquals("(I\\+)", GeneditsRegexUtils.translateRegex("I!+"));
        Assert.assertEquals("((([A-Za-z0-9])|(\\s)|(,)))", GeneditsRegexUtils.translateRegex("{x,b,!,}"));
        Assert.assertEquals("(\\d((\\d)|(\\.))*)", GeneditsRegexUtils.translateRegex("d{d,!.}*"));
    }

    @Test
    public void testActualMatches() {

        Assert.assertTrue("88".matches(GeneditsRegexUtils.translateRegex("dd")));
        Assert.assertTrue("17ff".matches(GeneditsRegexUtils.translateRegex("ddff")));
        Assert.assertTrue("404 899-7751".matches(GeneditsRegexUtils.translateRegex("404 ddd!-dddd")));
        Assert.assertTrue("ibb  z".matches(GeneditsRegexUtils.translateRegex("a{a,b}*")));
        Assert.assertTrue("C342".matches(GeneditsRegexUtils.translateRegex("C{341:9,384,50d,569,570}")));
        Assert.assertTrue("C508".matches(GeneditsRegexUtils.translateRegex("C{341:9,384,50d,569,570}")));
        Assert.assertTrue("001.7".matches(GeneditsRegexUtils.translateRegex("001:9.d")));
        Assert.assertTrue("30341-3724".matches(GeneditsRegexUtils.translateRegex("ddddd[[!-]dddd]")));
        Assert.assertFalse("30341".matches(GeneditsRegexUtils.translateRegex("ddddd[[!-]dddd]")));
        Assert.assertFalse("303413724".matches(GeneditsRegexUtils.translateRegex("ddddd[[!-]dddd]")));
        Assert.assertTrue("12:55 am".matches(GeneditsRegexUtils.translateRegex("{01:9,10:2}[!:]0:5d[b*[!am,pm]]")));
        Assert.assertTrue("15:55".matches(GeneditsRegexUtils.translateRegex("{0:1d,20:3}[!:]0:5d")));
        Assert.assertFalse("404899-7751".matches(GeneditsRegexUtils.translateRegex("404 ddd!-dddd")));
        Assert.assertFalse("404 A99-7751".matches(GeneditsRegexUtils.translateRegex("404 ddd!-dddd")));
        Assert.assertFalse("- ".matches(GeneditsRegexUtils.translateRegex("a{a,b}*")));
        Assert.assertFalse("C385".matches(GeneditsRegexUtils.translateRegex("C{341:9,384,50d,569,570}")));
        Assert.assertFalse("0017".matches(GeneditsRegexUtils.translateRegex("001:9.d")));
        Assert.assertFalse("303413724".matches(GeneditsRegexUtils.translateRegex("9dddd[[!-]dddd]")));
        Assert.assertFalse("13:55 am".matches(GeneditsRegexUtils.translateRegex("{01:9,10:2}[!:]0:5d[b*[!am,pm]]")));
        Assert.assertFalse("15:65".matches(GeneditsRegexUtils.translateRegex("{0:1d,20:3}[!:]0:5d")));

        Assert.assertTrue("I+".matches(GeneditsRegexUtils.translateRegex("I{+}")));
        Assert.assertTrue("I-".matches(GeneditsRegexUtils.translateRegex("I{-}")));
        Assert.assertTrue("I-".matches(GeneditsRegexUtils.translateRegex("I[-]")));
        Assert.assertTrue("I*".matches(GeneditsRegexUtils.translateRegex("I[*]")));
        Assert.assertTrue("I*".matches(GeneditsRegexUtils.translateRegex("I{*}")));
        Assert.assertTrue("I+".matches(GeneditsRegexUtils.translateRegex("I!+")));
        Assert.assertTrue("I+ ".matches(GeneditsRegexUtils.translateRegex("I!+b")));
        Assert.assertTrue("I-".matches(GeneditsRegexUtils.translateRegex("I!-")));
        Assert.assertTrue("I- ".matches(GeneditsRegexUtils.translateRegex("I!-b")));
        Assert.assertTrue("I*".matches(GeneditsRegexUtils.translateRegex("I!*")));
        Assert.assertTrue("I* ".matches(GeneditsRegexUtils.translateRegex("I!*b")));

        // real regex that contains spaces, all possible values
        Assert.assertTrue("pX".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0, p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0, p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("c0".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0, p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0I+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0I-".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0, p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0M+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0, p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0M-".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0, p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        // same tests, but I am playing around with the spaces (this has been verified in Genedits) - there can be spaces after the comma but not before
        Assert.assertTrue("p0I+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+},p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0I+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+}, p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0I+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+},  p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0I+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0 ,p0I{!+},p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0I+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0, p0I{!+},p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertFalse("p0I+".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+} ,p0I{!-},p0M{!+}, p0M{!-}")));
        // testing spaces in front/end of the regex (this has been verified in Genedits) - 
        Assert.assertTrue("pX".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+},p0I{!-},p0M{!+},p0M{!-}")));
        Assert.assertFalse("pX".matches(GeneditsRegexUtils.translateRegex(" pX,p0,c0,p0I{!+},p0I{!-},p0M{!+},p0M{!-}")));
        Assert.assertFalse("pX".matches(GeneditsRegexUtils.translateRegex("pX ,p0,c0,p0I{!+},p0I{!-},p0M{!+},p0M{!-}")));
        Assert.assertTrue("pX".matches(GeneditsRegexUtils.translateRegex("pX, p0,c0,p0I{!+},p0I{!-},p0M{!+},p0M{!-}")));
        Assert.assertTrue("p0M-".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+},p0I{!-},p0M{!+},p0M{!-}")));
        Assert.assertTrue("p0M-".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+},p0I{!-},p0M{!+},p0M{!-} ")));
        Assert.assertTrue("p0M-".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+},p0I{!-},p0M{!+}, p0M{!-}")));
        Assert.assertTrue("p0M-".matches(GeneditsRegexUtils.translateRegex("pX,p0,c0,p0I{!+},p0I{!-},p0M{!+} ,p0M{!-}")));

        String regex = GeneditsRegexUtils.translateRegex("a{b,a}*");
        Assert.assertFalse("".matches(regex));
        Assert.assertTrue("something".matches(regex));
        Assert.assertTrue("Something".matches(regex));
        Assert.assertTrue("sOmehtING".matches(regex));
        Assert.assertFalse(" omething".matches(regex));
        Assert.assertTrue("someth   ".matches(regex));
        Assert.assertTrue("some  ing".matches(regex));
        Assert.assertFalse(" . s.s . ".matches(regex));
        Assert.assertFalse("...".matches(regex));
        Assert.assertFalse("a..".matches(regex));
        Assert.assertTrue("abc".matches(regex));
        Assert.assertTrue("ab ".matches(regex));

        regex = GeneditsRegexUtils.translateRegex("a{.,(,)}+");
        Assert.assertTrue("a.()..(((.))".matches(regex));
        Assert.assertFalse("a.()..(((.))a".matches(regex));

        regex = GeneditsRegexUtils.translateRegex("{x,b,!,}");
        Assert.assertTrue("A".matches(regex));
        Assert.assertTrue("a".matches(regex));
        Assert.assertTrue("1".matches(regex));
        Assert.assertTrue(" ".matches(regex));
        Assert.assertTrue(",".matches(regex));
        Assert.assertFalse("+".matches(regex));
        Assert.assertFalse("-".matches(regex));
        Assert.assertFalse("_".matches(regex));
        Assert.assertFalse(")".matches(regex)); // EditsWriter returns true for this, looks like a bug to me!
        Assert.assertFalse("(".matches(regex)); // EditsWriter returns true for this, looks like a bug to me!
        Assert.assertFalse("{".matches(regex));
        Assert.assertFalse("}".matches(regex));
        Assert.assertFalse("[".matches(regex));
        Assert.assertFalse("]".matches(regex));

        regex = GeneditsRegexUtils.translateRegex("d{d,!.}*");
        Assert.assertTrue("1".matches(regex));
        Assert.assertTrue("11".matches(regex));
        Assert.assertTrue("1.1".matches(regex));
        Assert.assertTrue("1.1.".matches(regex));
        Assert.assertTrue("1..1".matches(regex));
        Assert.assertTrue("1..".matches(regex));
        Assert.assertFalse(".".matches(regex));
        Assert.assertFalse(".1".matches(regex));

        // this is from a real edit for street name to make sure it's left justified
        regex = GeneditsRegexUtils.translateRegex("@{?}*");
        Assert.assertFalse("".matches(regex));
        Assert.assertFalse(" ".matches(regex));
        Assert.assertTrue("A".matches(regex));
        Assert.assertFalse("  ".matches(regex));
        Assert.assertTrue("AB".matches(regex));
        Assert.assertTrue("A ".matches(regex));
        Assert.assertFalse(" A".matches(regex));
        Assert.assertFalse("   ".matches(regex));
        Assert.assertTrue("ABC".matches(regex));
        Assert.assertTrue("AB ".matches(regex));
        Assert.assertFalse(" AB".matches(regex));
        Assert.assertTrue("A B".matches(regex));

        // this is from a real edit for postal code
        regex = GeneditsRegexUtils.translateRegex("x{b,x}*");
        Assert.assertFalse("".matches(regex));
        Assert.assertFalse(" ".matches(regex));
        Assert.assertFalse("   ".matches(regex));
        Assert.assertTrue("12345".matches(regex));
        Assert.assertTrue("12345 ".matches(regex));
        Assert.assertFalse(" 12345".matches(regex));
        Assert.assertTrue("A1234".matches(regex));
        Assert.assertTrue("1234Z".matches(regex));
        Assert.assertTrue("abcABC".matches(regex));
        Assert.assertTrue("123 456".matches(regex));
        Assert.assertFalse(" 123456".matches(regex));
        Assert.assertFalse("1234!".matches(regex));
        Assert.assertFalse("12;34;56".matches(regex));
        Assert.assertFalse("<12345>".matches(regex));

        // this one was used for "Addr Current--Postal Code (COC)"
        regex = GeneditsRegexUtils.translateRegex("x{x}*{b}*");
        Assert.assertFalse("".matches(regex));
        Assert.assertFalse(" ".matches(regex));
        Assert.assertFalse("   ".matches(regex));
        Assert.assertTrue("12345".matches(regex));
        Assert.assertTrue("12345 ".matches(regex));
        Assert.assertFalse(" 12345".matches(regex));
        Assert.assertTrue("A1234".matches(regex));
        Assert.assertTrue("1234Z".matches(regex));
        Assert.assertTrue("abcABC".matches(regex));
        Assert.assertFalse("123 456".matches(regex));
        Assert.assertFalse(" 123456".matches(regex));
        Assert.assertFalse("1234!".matches(regex));
        Assert.assertFalse("12;34;56".matches(regex));
        Assert.assertFalse("<12345>".matches(regex));

        // this one was used for "Name--First (NPCR)"
        regex = GeneditsRegexUtils.translateRegex("a{a,b,-,'}*");
        Assert.assertFalse("".matches(regex));
        Assert.assertFalse(" ".matches(regex));
        Assert.assertFalse("   ".matches(regex));
        Assert.assertTrue("abc".matches(regex));
        Assert.assertTrue("ABC".matches(regex));
        Assert.assertTrue("AB ".matches(regex));
        Assert.assertFalse(" AB".matches(regex));
        Assert.assertTrue("A BC".matches(regex));
        Assert.assertTrue("A'BC".matches(regex));
        Assert.assertTrue("A-BC".matches(regex));
        Assert.assertTrue("ABC ".matches(regex));
        Assert.assertFalse(" ABC".matches(regex));
        Assert.assertFalse("'ABC".matches(regex));
        Assert.assertFalse("AB_C".matches(regex));
        Assert.assertFalse("AB<C>".matches(regex));

        // this one was taken from "Secondary Diagnosis 1 (COC)"
        regex = GeneditsRegexUtils.translateRegex("uxx{x}*{b}*");
        Assert.assertFalse("".matches(regex));
        Assert.assertFalse(" ".matches(regex));
        Assert.assertFalse("A".matches(regex));
        Assert.assertFalse("x".matches(regex));
        Assert.assertFalse("1".matches(regex));
        Assert.assertFalse("a ".matches(regex));
        Assert.assertFalse(" a".matches(regex));
        Assert.assertFalse("x ".matches(regex));
        Assert.assertFalse("1 ".matches(regex));
        Assert.assertFalse("A ".matches(regex));
        Assert.assertFalse("A1".matches(regex));
        Assert.assertFalse("Aa".matches(regex));
        Assert.assertFalse("A ".matches(regex));
        Assert.assertTrue("ABC".matches(regex));
        Assert.assertTrue("Abc".matches(regex));
        Assert.assertTrue("A12".matches(regex));
        Assert.assertTrue("X12".matches(regex));
        Assert.assertTrue("Xab".matches(regex));
        Assert.assertFalse("abc".matches(regex));
        Assert.assertFalse("aBC".matches(regex));
        Assert.assertFalse("123".matches(regex));
        Assert.assertTrue("Abc ".matches(regex));
        Assert.assertTrue("Xab ".matches(regex));
        Assert.assertTrue("X12 ".matches(regex));
        Assert.assertTrue("ABCD".matches(regex));
        Assert.assertFalse("ABC'".matches(regex));
        Assert.assertFalse("ABC-2".matches(regex));
        Assert.assertTrue("Abc123".matches(regex));
        Assert.assertTrue("Abc123 ".matches(regex));
        Assert.assertFalse("ABC abc".matches(regex));

        // this one was also taken from "Secondary Diagnosis 1 (COC)"
        regex = GeneditsRegexUtils.translateRegex("{A,B,E,G:P,R,S}xx{x}*{b}*");
        Assert.assertFalse("".matches(regex));
        Assert.assertFalse(" ".matches(regex));
        Assert.assertFalse("A".matches(regex));
        Assert.assertFalse("Aa".matches(regex));
        Assert.assertFalse("A1".matches(regex));
        Assert.assertFalse("C1".matches(regex));
        Assert.assertFalse("Aa ".matches(regex));
        Assert.assertFalse("A a".matches(regex));
        Assert.assertFalse("A1 ".matches(regex));
        Assert.assertFalse("A 1".matches(regex));
        Assert.assertFalse("abc".matches(regex));
        Assert.assertFalse("a12".matches(regex));
        Assert.assertFalse("C12".matches(regex));
        Assert.assertFalse("Cab".matches(regex));
        Assert.assertTrue("A12".matches(regex));
        Assert.assertTrue("B12".matches(regex));
        Assert.assertTrue("G12".matches(regex));
        Assert.assertTrue("I12".matches(regex));
        Assert.assertTrue("R12".matches(regex));
        Assert.assertTrue("S12".matches(regex));
        Assert.assertFalse("X12".matches(regex));
        Assert.assertTrue("Sab".matches(regex));
        Assert.assertTrue("SAB".matches(regex));
        Assert.assertFalse("123".matches(regex));
        Assert.assertFalse("C1  ".matches(regex));
        Assert.assertFalse("123 ".matches(regex));
        Assert.assertFalse("a12 ".matches(regex));
        Assert.assertTrue("SAB  ".matches(regex));
        Assert.assertTrue("SAB123".matches(regex));
        Assert.assertTrue("SABabc".matches(regex));
        Assert.assertTrue("SAB123 ".matches(regex));
        Assert.assertTrue("SABabc ".matches(regex));

        // this one was taken from "Edit Over-rides (SEER REVIEWFL)" in NAACCR Call for Data metafile
        regex = GeneditsRegexUtils.translateRegex("1,b");
        Assert.assertFalse("".matches(regex));
        Assert.assertTrue(" ".matches(regex));
        Assert.assertTrue("1".matches(regex));
        Assert.assertFalse("2".matches(regex));

        // this one was also taken from "Edit Over-rides (SEER REVIEWFL)" in NAACCR Call for Data metafile
        regex = GeneditsRegexUtils.translateRegex("1:3,b");
        Assert.assertFalse("".matches(regex));
        Assert.assertTrue(" ".matches(regex));
        Assert.assertTrue("1".matches(regex));
        Assert.assertTrue("2".matches(regex));
        Assert.assertFalse("4".matches(regex));

        // this one was taken from "EOD--Old 4 digit (SEER IF264DIG_P1)"
        regex = GeneditsRegexUtils.translateRegex("[bb,dd]{b,d}{b,d}");
        Assert.assertFalse("".matches(regex));
        Assert.assertFalse(" ".matches(regex));
        Assert.assertFalse("0".matches(regex));
        Assert.assertFalse("  ".matches(regex));
        Assert.assertFalse("00".matches(regex));
        Assert.assertFalse("0 ".matches(regex));
        Assert.assertFalse(" 0".matches(regex));
        Assert.assertFalse("   ".matches(regex));
        Assert.assertFalse("000".matches(regex));
        Assert.assertFalse("00 ".matches(regex));
        Assert.assertFalse(" 00".matches(regex));
        Assert.assertFalse("0  ".matches(regex));
        Assert.assertFalse("  0".matches(regex));
        Assert.assertFalse("   ".matches(regex));
        Assert.assertTrue("0000".matches(regex));
        Assert.assertTrue("000 ".matches(regex));
        Assert.assertTrue("00  ".matches(regex));
        Assert.assertFalse("0   ".matches(regex));
        Assert.assertFalse(" 000".matches(regex));
        Assert.assertTrue("  00".matches(regex));
        Assert.assertTrue("   0".matches(regex));

    }
}
