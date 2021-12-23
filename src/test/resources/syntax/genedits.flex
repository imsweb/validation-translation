/*
  Flex definition of the Genedits syntax.
*/

package com.imsweb.validation.translation.language;
      
%%

%class GeneditsLexer
%public
%line
%column
%cup
%ignorecase

%{
    StringBuffer commentBuffer = new StringBuffer();
%}

LineTerminator = \r?\n
EmptyLine = {LineTerminator}([ ]*{LineTerminator})+
StringLiteral = "\"" [^\"]* "\""
CharacterLiteral = '[^\"]'
NumberLiteral = [0-9]+
Identifier = [:jletter:][:jletterdigit:]*

%states IN_COMMENT

%%


<YYINITIAL> {

  /* comments */
  "/*"                         { commentBuffer.setLength(0); commentBuffer.append(yytext()); yybegin(IN_COMMENT); }

  /* keywords */
  "IF"                         { return new java_cup.runtime.Symbol(GeneditsTokenType.IF, yyline, yycolumn); }
  ({EmptyLine}[ ]*)?"ELSE"     { return new java_cup.runtime.Symbol(GeneditsTokenType.ELSE, yyline, yycolumn); }
  "WHILE"                      { return new java_cup.runtime.Symbol(GeneditsTokenType.WHILE, yyline, yycolumn); }
  "RETURN"                     { return new java_cup.runtime.Symbol(GeneditsTokenType.RETURN, yyline, yycolumn); }
  "FUNCTION"                   { return new java_cup.runtime.Symbol(GeneditsTokenType.FUNCTION, yyline, yycolumn); }

  /* types */
  "int" |
  "long" |
  "char" |
  "tablevar"
                               { return new java_cup.runtime.Symbol(GeneditsTokenType.TYPE, yyline, yycolumn, yytext()); }
  
  /* separators */
  ({EmptyLine}[ ]*)?"("        { return new java_cup.runtime.Symbol(GeneditsTokenType.LPAREN, yyline, yycolumn); }
  ({EmptyLine}[ ]*)?")"        { return new java_cup.runtime.Symbol(GeneditsTokenType.RPAREN, yyline, yycolumn); }
  ({EmptyLine}[ ]*)?"{"        { return new java_cup.runtime.Symbol(GeneditsTokenType.LBRACE, yyline, yycolumn); }
  ({EmptyLine}[ ]*)?"}"        { return new java_cup.runtime.Symbol(GeneditsTokenType.RBRACE, yyline, yycolumn); }
  "["                          { return new java_cup.runtime.Symbol(GeneditsTokenType.LBRACK, yyline, yycolumn); }
  "]"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.RBRACK, yyline, yycolumn); }

  /* operators */
  "+"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.ARITHMETHIC_OP, yyline, yycolumn, "+"); }
  "-"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.MINUS_OP, yyline, yycolumn, "-"); } /* minus can be both binary or UNARY op */
  "*"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.ARITHMETHIC_OP, yyline, yycolumn, "*"); }
  "/"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.ARITHMETHIC_OP, yyline, yycolumn, "/"); }
  "%"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.ARITHMETHIC_OP, yyline, yycolumn, "%"); }
  "=="                         { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, "=="); }
  "!="                         { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, "!="); }
  ">="                         { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, ">="); }
  "<="                         { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, "<="); }
  ">"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, ">"); }
  "<"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, "<"); }
  ({EmptyLine}[ ]*)?"&&"({EmptyLine}[ ]*)? | ({EmptyLine}[ ]*)?"AND"({EmptyLine}[ ]*)?                 
                               { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, "&&"); }
  ({EmptyLine}[ ]*)?"||"({EmptyLine}[ ]*)? | ({EmptyLine}[ ]*)?"OR"({EmptyLine}[ ]*)?                  
                               { return new java_cup.runtime.Symbol(GeneditsTokenType.BINARY_OP, yyline, yycolumn, "||"); }
  "!" | "NOT"                  { return new java_cup.runtime.Symbol(GeneditsTokenType.UNARY_OP, yyline, yycolumn, "!"); }
  "="                          { return new java_cup.runtime.Symbol(GeneditsTokenType.EQ, yyline, yycolumn); }
  "#S"                         { return new java_cup.runtime.Symbol(GeneditsTokenType.SPOUND, yyline, yycolumn); }
  "#L"                         { return new java_cup.runtime.Symbol(GeneditsTokenType.LPOUND, yyline, yycolumn); }
  
  ";"                          { return new java_cup.runtime.Symbol(GeneditsTokenType.SEMICOLON, yyline, yycolumn); }
  ","                          { return new java_cup.runtime.Symbol(GeneditsTokenType.COMMA, yyline, yycolumn); }

  /* string literals */  
  {StringLiteral}              { return new java_cup.runtime.Symbol(GeneditsTokenType.STRING, yyline, yycolumn, yytext()); }

  /* character literal */
  {CharacterLiteral}		   { return new java_cup.runtime.Symbol(GeneditsTokenType.STRING, yyline, yycolumn, yytext()); }
  
  /* number literals */
  {NumberLiteral}              { return new java_cup.runtime.Symbol(GeneditsTokenType.NUMBER, yyline, yycolumn, Integer.valueOf(yytext())); }

  /* constants */
  "TRUE" |
  "FALSE" |
  "PASS" |
  "FAIL" |
  "DT_ERROR" |
  "DT_EMPTY" |
  "DT_UNKNOWN" |
  "DT_DAY_EMPTY" |
  "DT_MONTH_EMPTY" |
  "DT_MIN" |
  "DT_MAX" |
  "DT_EXACT" |
  "DT_TODAY" |
  "BOTH" |
  "LEFT" |
  "RIGHT"
                               { return new java_cup.runtime.Symbol(GeneditsTokenType.CONSTANT, yyline, yycolumn, yytext()); }
  
  /* identifiers */
  {Identifier}                 { return new java_cup.runtime.Symbol(GeneditsTokenType.IDENTIFIER, yyline, yycolumn, yytext()); }
  
  {EmptyLine}                  { return new java_cup.runtime.Symbol(GeneditsTokenType.EMPTY_LINE, yyline, yycolumn); }
}

<IN_COMMENT> {
  "*/"      { yybegin(YYINITIAL); commentBuffer.append(yytext()); return new java_cup.runtime.Symbol(GeneditsTokenType.COMMENT, yyline, yycolumn, commentBuffer.toString()); }
  [^*\n]+   { commentBuffer.append(yytext()); }
  "*"       { commentBuffer.append(yytext()); }
  \n        { commentBuffer.append(yytext()); }
}

/* error fallback */
.|\n                           { }
<<EOF>>                        { return null; }
