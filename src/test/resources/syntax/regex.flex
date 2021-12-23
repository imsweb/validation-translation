package com.imsweb.validation.translation.language.regex;
      
%%

%class GeneditsRegexLexer
%public
%line
%column
%cup
   
%%

<YYINITIAL> {
    "x"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.ALPHA_NUMERIC, yytext()); }
    "@"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.NO_BLANK, yytext()); }
    "?"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.CHAR, yytext()); }
    "a"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.ALPHA, yytext()); }
    "u"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.UPPER, yytext()); }
    "l"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.LOWER, yytext()); }
    "b"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.BLANK, yytext()); }
    "d"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.DIGIT, yytext()); }
    "!"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.ESCAPE, yytext()); }
    ":"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.RANGE, yytext()); }
    "{"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.LEFT_BRACE, yytext()); }    
    "}"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.RIGHT_BRACE, yytext()); }
    "["                { return new java_cup.runtime.Symbol(GeneditsRegexToken.LEFT_BRACKET, yytext()); }
    "]"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.RIGHT_BRACKET, yytext()); }
    "("                { return new java_cup.runtime.Symbol(GeneditsRegexToken.LEFT_PAR, yytext()); }
    ")"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.RIGHT_PAR, yytext()); }
    "+"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.PLUS, yytext()); }
    "*"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.STAR, yytext()); }
    "-"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.DASH, yytext()); }
    ","                { return new java_cup.runtime.Symbol(GeneditsRegexToken.COMMA, yytext()); }
    "|"                { return new java_cup.runtime.Symbol(GeneditsRegexToken.PIPE, yytext()); }
    "."                { return new java_cup.runtime.Symbol(GeneditsRegexToken.PERIOD, yytext()); }
    .                  { return new java_cup.runtime.Symbol(GeneditsRegexToken.LITERAL, yytext()); }
}

<<EOF>>                { return null; }
