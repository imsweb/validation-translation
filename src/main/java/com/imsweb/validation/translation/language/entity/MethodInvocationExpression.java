/*
 * Copyright (C) 2010 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.language.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.imsweb.validation.translation.EditTranslationContext;
import com.imsweb.validation.translation.TranslationTable;
import com.imsweb.validation.translation.TranslationTableIndex;
import com.imsweb.validation.translation.language.regex.GeneditsRegexUtils;
import com.imsweb.validation.translation.metafile.MetafileEdit;
import com.imsweb.validation.translation.metafile.MetafileMessage;
import com.imsweb.validation.translation.metafile.MetafileTable;
import com.imsweb.validation.translation.metafile.MetafileUtils;

/**
 * The lookup calls are more complicated than the other methods and they changed in the new Genedits5 framwork. So here is some
 * information about how the used to work and how they work now.
 * &lt;p&gt;
 * **************************************************************************************
 * GENEDITS4 (old framework, not supported by the translation anymore)
 * **************************************************************************************
 * &lt;p&gt;
 * LOOKUP(value, table/index) -&gt; GEN_LOOKUP(Object value, List&lt;List&lt;String&gt;&gt; table, Object indexObj, Map&lt;Integer, char[]&gt; tableVars)
 * - table needed if a table is provided as argument, or if tablevar are declared
 * - index needed if an index is provided as argument (if only table, then the value is compared to concatenation of all columns)
 * RLOOKUP(value, index) -&gt; GEN_RLOOKUP(Object value, List&lt;List&lt;String&gt;&gt; table, Object indexObj, Map&lt;Integer, char[]&gt; tableVars)
 * - table needed if tablevar are declared
 * - index is always needed
 * ILOOKUP(value, index) -&gt; GEN_ILOOKUP(Object value, Object indexObj)
 * - table is never needed
 * - index is always needed
 * BINLOOKUP(table, col/row) -&gt; GEN_BINLOOKUP(List&lt;List&lt;Integer&gt;&gt; table, Object rowObj, Object colObj)
 * - table is always needed
 * - index is never needed
 * &lt;p&gt;
 * **************************************************************************************
 * GENEDITS5 (new framework, this is what this code produces now)
 * **************************************************************************************
 * &lt;p&gt;
 * LOOKUP(value, table/index) -&gt; GEN_LOOKUP(Object value, ContextTable table, ContextTableIndex index, Map&lt;String, char[]&gt; tableVars)
 * - table and index always required (index is actually a column in the new SQL-based tables)
 * RLOOKUP(value, table/index) -&gt; GEN_RLOOKUP(Object value, ContextTable table, ContextTableIndex index, Map&lt;String, char[]&gt; tableVars)
 * - table and index always required (index is actually a column in the new SQL-based tables)
 * ILOOKUP(value, index) -&gt; GEN_ILOOKUP(Object value, ContextTable table, ContextTableIndex index, Map&lt;String, char[]&gt; tableVars)
 * - table and index always required (index is actually a column in the new SQL-based tables)
 * SQLLOOKUP(table, columns, value) -&gt; GEN_SQLLOOKUP(ContextTable table, ContextTableIndex index, Object value, Map&lt;String, char[]&gt; tableVars)
 * - table and index always required (the columns(s) describe the index)
 * SQLRANGELOOKUP(table, columns, value) -&gt; GEN_SQLRANGELOOKUP(ContextTable table, ContextTableIndex index, Object value, Map&lt;String, char[]&gt; tableVars)
 * - table and index always required (the columns(s) describe the index)
 * &lt;p&gt;
 * possible syntax for the tables/indexes:
 * - TABLE
 * - TABLE.DBF
 * - TABLE.INDEX
 * - TABLE.DBF.INDEX
 * Note that the new framework doesn't use "DBF" anymore, so this translation now removes that suffix if it sees it.
 * &lt;p&gt;
 * New syntax for tables (first row is always the headers):
 * [['header'], ['val1'], ['val2'], ['val3']]
 * [['header1', 'header2'], ['val1', 'val2']]
 * New syntax for indexes:
 * ['table': 'tableX', 'columns' : 'header1']
 * ['table': 'tableX', 'columns' : 'header1,header2']
 * &lt;p&gt;
 * The rule to generate the index names is {TABLE-NAME}_{CONCATENATED-COLUMNS}, so something like TABLE_COL1COL2
 */
public class MethodInvocationExpression extends Expression {

    private static final Logger _LOG = LogManager.getLogger(MethodInvocationExpression.class);

    private final String _originalMethodName;

    private final String _upperCasedMethodName;

    private final ArgumentList _arguments;

    /**
     * The following methods will have the binding added as an extra parameter
     */
    private static final Set<String> _REQUIRE_BINDING = new HashSet<>();

    static {
        _REQUIRE_BINDING.add("ERROR_TEXT");
        _REQUIRE_BINDING.add("ERROR_MSG");
        _REQUIRE_BINDING.add("SAVE_TEXT");
        _REQUIRE_BINDING.add("SAVE_ERROR_TEXT");
        _REQUIRE_BINDING.add("SAVE_WARNING_TEXT");
        _REQUIRE_BINDING.add("SET_WARNING");
        _REQUIRE_BINDING.add("SET_ERROR");
        _REQUIRE_BINDING.add("VALID_DATE_IOP");
        _REQUIRE_BINDING.add("ALLOW_FUTURE_DATE_IOP");
        _REQUIRE_BINDING.add("DATE_YEAR_IOP");
        _REQUIRE_BINDING.add("DATE_MONTH_IOP");
        _REQUIRE_BINDING.add("DATE_DAY_IOP");
        _REQUIRE_BINDING.add("DATECMP_IOP");
        _REQUIRE_BINDING.add("YEARDIFF_IOP");
        _REQUIRE_BINDING.add("YEARINTERVAL_IOP");
        _REQUIRE_BINDING.add("MONTHDIFF_IOP");
        _REQUIRE_BINDING.add("MONTHINTERVAL_IOP");
        _REQUIRE_BINDING.add("DAYDIFF_IOP");
        _REQUIRE_BINDING.add("DAYINTERVAL_IOP");
        _REQUIRE_BINDING.add("USR2");
        _REQUIRE_BINDING.add("USR4");
    }

    /**
     * All Genedits methods
     */
    private static final Set<String> _ALL_METHODS = new HashSet<>();

    static {
        _ALL_METHODS.addAll(_REQUIRE_BINDING);
        _ALL_METHODS.add("EMPTY");
        _ALL_METHODS.add("VAL");
        _ALL_METHODS.add("TRIM");
        _ALL_METHODS.add("STRLEN");
        _ALL_METHODS.add("INLIST");
        _ALL_METHODS.add("MATCH");
        _ALL_METHODS.add("ILOOKUP");
        _ALL_METHODS.add("LOOKUP");
        _ALL_METHODS.add("RLOOKUP");
        _ALL_METHODS.add("SQLLOOKUP");
        _ALL_METHODS.add("SQLRANGELOOKUP");
        _ALL_METHODS.add("BINLOOKUP");
        _ALL_METHODS.add("SUBSTR");
        _ALL_METHODS.add("STRCPY");
        _ALL_METHODS.add("STRCAT");
        _ALL_METHODS.add("STRCMP");
        _ALL_METHODS.add("FMTSTR");
        _ALL_METHODS.add("AT");
        _ALL_METHODS.add("JUSTIFIED");
        _ALL_METHODS.add("RIGHT");
        _ALL_METHODS.add("LEFT");
        _ALL_METHODS.add("LOWER");
        _ALL_METHODS.add("UPPER");
        _ALL_METHODS.add("USER2");
        _ALL_METHODS.add("USER4");
        _ALL_METHODS.add("GETFIELD");
        _ALL_METHODS.add("PUTFIELD");
        _ALL_METHODS.add("GETVAR");
        _ALL_METHODS.add("SETVAR");
        _ALL_METHODS.add("NAMEEXPR");
        _ALL_METHODS.add("EXTERNALDLL");
        _ALL_METHODS.add("USR2");
        _ALL_METHODS.add("USR4");
        _ALL_METHODS.add("NOOP"); // not really a Genedits method, used internally
    }

    /**
     * Not supported Genedits methods
     */
    private static final Set<String> _NON_SUPPORTED_METHODS = new HashSet<>();

    static {
        _NON_SUPPORTED_METHODS.add("GETFIELD");
        _NON_SUPPORTED_METHODS.add("PUTFIELD");
        _NON_SUPPORTED_METHODS.add("GETVAR");
        _NON_SUPPORTED_METHODS.add("SETVAR");
        _NON_SUPPORTED_METHODS.add("NAMEEXPR");
    }

    private static final Set<String> _PARSED_USED_TABLES = new HashSet<>();

    public MethodInvocationExpression(String methodName, ArgumentList arguments) {
        _LOG.debug("new MethodInvocationExpression(" + methodName + ", " + arguments + ")");

        _originalMethodName = methodName;
        _upperCasedMethodName = methodName.toUpperCase();
        if ("BINLOOKUP".equals(_upperCasedMethodName))
            throw new RuntimeException("BINLOOKUP was supposed to be retired in new framework!");
        _arguments = arguments;

        if (_upperCasedMethodName.endsWith("LOOKUP")) {
            String rawTableName = null;
            if (_upperCasedMethodName.equals("LOOKUP") || _upperCasedMethodName.equals("RLOOKUP") || _upperCasedMethodName.equals("ILOOKUP"))
                rawTableName = ((LiteralExpression)arguments.getExpressions().get(1)).getLiteral();
            else if (_upperCasedMethodName.equals("SQLLOOKUP") || _upperCasedMethodName.equals("SQLRANGELOOKUP"))
                rawTableName = ((LiteralExpression)arguments.getExpressions().get(0)).getLiteral();
            if (rawTableName != null)
                _PARSED_USED_TABLES.add(StringUtils.split(rawTableName.substring(1, rawTableName.length() - 1).toUpperCase().replace(".DBF", ""), '.')[0]);
        }
    }

    @Override
    @SuppressWarnings("IfCanBeSwitch")
    public void generateGroovy(StringBuilder buf, EditTranslationContext tContext) {

        if (isInternalMethod(_upperCasedMethodName)) {
            if (_NON_SUPPORTED_METHODS.contains(_upperCasedMethodName))
                throw new RuntimeException("This edit uses a function call that has not been implemented: " + _upperCasedMethodName);

            buf.append("Functions.GEN_").append(_upperCasedMethodName).append("(");

            // lookup calls are special/complex, let's deal with them separately...
            if (_upperCasedMethodName.equals("LOOKUP") || _upperCasedMethodName.equals("RLOOKUP") || _upperCasedMethodName.equals("ILOOKUP"))
                generateArgumentsForOldLookups(tContext, buf, _arguments.getExpressions());
            else if (_upperCasedMethodName.equals("SQLLOOKUP") || _upperCasedMethodName.equals("SQLRANGELOOKUP"))
                generateArgumentsForNewLookups(tContext, buf, _arguments.getExpressions());
            else {
                StringBuilder argBuf = new StringBuilder();

                // add extra binding parameter to some of the calls
                if (_REQUIRE_BINDING.contains(_upperCasedMethodName))
                    argBuf.append("binding, ");

                // iterate over the arguments and translate them one by one
                for (int i = 0; i < _arguments.getExpressions().size(); i++) {
                    StringBuilder exp = new StringBuilder();
                    _arguments.getExpressions().get(i).generateGroovy(exp, tContext);
                    String expStr = exp.toString();

                    // special case for INLIST and MATCH, translate the regex
                    if ((i == 2 && "INLIST".equals(_upperCasedMethodName)) || (i == 1 && "MATCH".equals(_upperCasedMethodName)))
                        argBuf.append("\"").append(GeneditsRegexUtils.translateRegex(expStr.substring(1, expStr.length() - 1)).replace("\\", "\\\\")).append("\", ");
                        // some of the functions use message numbers, replace those with the actual message
                    else if (("ERROR_MSG".equals(_upperCasedMethodName) || "SAVE_ERROR_TEXT".equals(_upperCasedMethodName) || "SAVE_WARNING_TEXT".equals(_upperCasedMethodName) || "SET_WARNING".equals(
                            _upperCasedMethodName) || "SET_ERROR".equals(_upperCasedMethodName) || "ERROR_TEXT".equals(_upperCasedMethodName) || "SAVE_TEXT".equals(_upperCasedMethodName) ||
                            "USR2".equals(_upperCasedMethodName)) && i == 0) {

                        // ERROR_TEXT and SAVE_TEXT use raw messages; others reference a message number
                        if ("ERROR_TEXT".equals(_upperCasedMethodName) || "SAVE_TEXT".equals(_upperCasedMethodName)) {
                            if (expStr.startsWith("\"") && expStr.endsWith("\"")) {
                                String msgValue = expStr.substring(1, expStr.length() - 1);

                                // replace %F by the field name and %V by the value
                                argBuf.append("'").append(MetafileUtils.fixMessage(msgValue, tContext.getEdit(), false).replace("'", "\\'")).append("', ");
                            }
                            else // must be a local variable
                                argBuf.append(expStr).append(", ");

                        }
                        else {
                            MetafileMessage msg = null;
                            for (MetafileMessage m : tContext.getEdit().getMessages())
                                if (m.getNumber() != null && m.getNumber().toString().equals(expStr))
                                    msg = m;
                            // I found a bug in some metafile where some edits were not properly linked to their used messages, this addresses that weird case...
                            if (msg == null && tContext.getMetafile() != null && tContext.getMetafile().getMessages() != null)
                                msg = tContext.getMetafile().getMessages().stream().filter(m -> m.getNumber().toString().equals(expStr)).findFirst().orElse(null);
                            if (msg == null)
                                throw new RuntimeException("Unable to find message #" + expStr + " on " + tContext.getEdit().getName());

                            // replace %F by the field name and %V by the value
                            argBuf.append("'").append(MetafileUtils.fixMessage(msg.getMessage(), tContext.getEdit(), false).replace("'", "\\'")).append("', ");
                        }

                    }
                    else
                        argBuf.append(expStr).append(", ");
                }

                if (argBuf.length() > 0) {
                    buf.append(argBuf);
                    buf.setLength(buf.length() - 2);
                }
            }
            buf.append(")");
        }
        else {
            // the method is an internal function; let's replace the call with the entire logic of the function; I hate doing that, but they have a very peculiar way of defining functions:
            // functions can access and modify variables that are defined in the main method (so those variables are global); that doesn't work well with statically compiled Groovy...
            Statements functionStatements = tContext.getFunctions().get(_upperCasedMethodName);
            if (functionStatements == null)
                throw new RuntimeException("Unable to find function " + _originalMethodName);
            tContext.setCurrentFunction(_upperCasedMethodName);
            tContext.setCurrentVariableSuffix(String.valueOf(FunctionStatement.getNextFunctionCounter()));
            buf.append(tContext.getCurrentFunctionIndentation()).append("// *** START function call ").append(_originalMethodName).append("()\n");
            functionStatements.generateGroovy(buf, tContext, tContext.getCurrentFunctionIndentation());
            buf.append(tContext.getCurrentFunctionIndentation()).append("// *** END function call ").append(_originalMethodName).append(
                    "()"); // no trailing new line because it will be added by the code handling a method call...
            tContext.setCurrentVariableSuffix(null);
            tContext.setCurrentFunction(null);
        }
    }

    private void generateArgumentsForOldLookups(EditTranslationContext tContext, StringBuilder buf, List<Expression> arguments) {
        // (value, table/index) -> (Object value, ContextTable table, ContextTableIndex index, Map<String, char[]> tableVars)

        if (arguments.size() != 2)
            throw new RuntimeException("Was expecting 2 arguments for old lookup call, but got " + arguments.size());
        if (tContext.getTables() == null)
            throw new RuntimeException("Table information is required for translating lookup calls!");

        // value is just translated the "normal" way, by calling generateGroovy on it
        arguments.get(0).generateGroovy(buf, tContext);
        buf.append(", ");

        // table/index is a string, so we have to remove the quotes, and also remove the deprecated DBF if it's there
        StringBuilder tableAndIndexBuf = new StringBuilder();
        arguments.get(1).generateGroovy(tableAndIndexBuf, tContext);
        String tableAndIndex = tableAndIndexBuf.substring(1, tableAndIndexBuf.length() - 1).toUpperCase().replace(".DBF", "");

        // in the new framework, they replaced the concept of index by columns; they did that by adding new columns for the existing old index;
        // that means doing a lookup on just a table is not really supported anymore (if it is, then it would mean doing a lookup on an index that
        // is the concatenation of all the columns, which I could support, but I don't think anybody is doing it, so for now it won't be supported)
        int idx = tableAndIndex.indexOf('.');
        if (idx == -1)
            throw new RuntimeException("Making lookup calls against a table without an index is not supported; found " + tableAndIndex);

        // write the table
        String tableName = tableAndIndex.substring(0, idx);
        MetafileTable table = tContext.getTables().get(tableName);
        if (table == null)
            throw new RuntimeException("Unable to get table " + tableName);
        //buf.append("(ContextTable)").append(addPrefix(tableName, tContext)).append(", "); // added a cast to help with static types
        buf.append(addPrefix(tableName, tContext)).append(", "); // added a cast to help with static types

        // write the index representing the column to use
        String columnName = tableAndIndex.substring(idx + 1);
        if (!table.getHeaders().contains(columnName))
            throw new RuntimeException("Unable to get column " + columnName + " on table " + tableName + "; available columns: " + table.getHeaders());
        String indexName = tableName + "_" + columnName;
        //buf.append("(ContextTableIndex)").append(addPrefix(indexName, tContext)).append(", "); // added a cast to help with static types
        buf.append(addPrefix(indexName, tContext)).append(", "); // added a cast to help with static types
        List<String> indexColumns = Collections.singletonList(columnName);

        // write the tablevars; those a written as a Groovy map where the keys are the column headers and the values are references to the declared tablevars
        List<String> tablevarMappings = getTablevarMappings(tContext.getEdit(), table, true);
        if (!tablevarMappings.isEmpty())
            buf.append("[").append(StringUtils.join(tablevarMappings, ", ")).append("]");
        else
            buf.append("[:]");

        // last but not least, let the translation know that a table/index was used
        recordTableUsage(tContext, tableName, indexName, indexColumns, getTablevarMappings(tContext.getEdit(), table, false));

    }

    private void generateArgumentsForNewLookups(EditTranslationContext tContext, StringBuilder buf, List<Expression> arguments) {
        // (table, columns, value) -> (ContextTable table, ContextTableIndex index, Object value, Map<String, char[]> tableVars)

        if (arguments.size() != 3)
            throw new RuntimeException("Was expecting 3 arguments for new lookup call, but got " + arguments.size());
        if (tContext.getTables() == null)
            throw new RuntimeException("Table information is required for translating lookup calls!");

        // table is a string, so we have to remove the quotes, and also remove the deprecated DBF if it's there
        StringBuilder tableNameBuf = new StringBuilder();
        arguments.get(0).generateGroovy(tableNameBuf, tContext);
        String tableName = tableNameBuf.substring(1, tableNameBuf.length() - 1).toUpperCase().replace(".DBF", "");
        MetafileTable table = tContext.getTables().get(tableName);
        if (table == null)
            throw new RuntimeException("Unable to get table " + tableName);
        buf.append("(com.imsweb.validation.entities.ContextTable)").append(addPrefix(tableName, tContext)).append(", "); // added a cast to help with static types

        // columns are strings, we also have to remove the quotes; we also have to create an index name ouf of the columns; convention is to use the
        // concatenate column names so something like TABLE_COL1COL2
        StringBuilder columnNamesBuf = new StringBuilder();
        arguments.get(1).generateGroovy(columnNamesBuf, tContext);
        String[] columnNames = StringUtils.split(columnNamesBuf.substring(1, columnNamesBuf.length() - 1).toUpperCase().replace(" ", ""), ',');
        for (String columnName : columnNames)
            if (!table.getHeaders().contains(columnName))
                throw new RuntimeException("Unable to get column " + columnName + " on table " + tableName + "; available columns: " + table.getHeaders());
        String indexName = tableName + "_" + StringUtils.join(columnNames);
        buf.append("(com.imsweb.validation.entities.ContextTableIndex)").append(addPrefix(indexName, tContext)).append(", "); // added a cast to help with static types
        List<String> indexColumns = Arrays.asList(columnNames);

        // value is just translated the "normal" way, by calling generateGroovy on it
        arguments.get(2).generateGroovy(buf, tContext);
        buf.append(", ");

        // write the tablevars; those a written as a Groovy map where the keys are the column headers and the values are references to the declared tablevars
        List<String> tablevarMappings = getTablevarMappings(tContext.getEdit(), table, true);
        if (!tablevarMappings.isEmpty())
            buf.append("[").append(StringUtils.join(tablevarMappings, ", ")).append("]");
        else
            buf.append("[:]");

        // last but not least, let the translation know that a table/index was used
        recordTableUsage(tContext, tableName, indexName, indexColumns, getTablevarMappings(tContext.getEdit(), table, false));
    }

    private List<String> getTablevarMappings(MetafileEdit edit, MetafileTable table, boolean formatIntoMap) {
        List<String> result = new ArrayList<>();
        for (String header : table.getHeaders()) {
            for (String declaredVar : DeclarationStatement.getDeclaredTableVars()) {
                // this is a poor way to implement this optimization but the idea is that if a tablevar is declared but not used anywhere, there is no need to really use it...
                if (declaredVar.equalsIgnoreCase(header) && StringUtils.countMatches(edit.getLogic().toLowerCase(), declaredVar.toLowerCase()) > 1) {
                    if (formatIntoMap)
                        result.add("'" + header + "':" + declaredVar);
                    else
                        result.add(header);
                    break;
                }
            }
        }
        return result;
    }

    private void recordTableUsage(EditTranslationContext tContext, String tableName, String indexName, List<String> indexColumns, List<String> tableVarColumns) {
        // last but not least, let the translation know that a table/index was used (for old lookups, the index name is also the column name)
        TranslationTable tableDto = tContext.getUsedTablesAndIndexes().computeIfAbsent(tableName, TranslationTable::new);
        TranslationTableIndex indexDto = tableDto.getIndexes().get(indexName);
        if (indexDto == null) {
            indexDto = new TranslationTableIndex();
            indexDto.setName(indexName);
            indexDto.setColumns(indexColumns);
            tableDto.getIndexes().put(indexName, indexDto);
        }
        else if (!indexDto.getColumns().equals(indexColumns))
            throw new RuntimeException("Got two def of same idx on " + tableName + ": " + indexDto.getColumns() + " for " + indexDto.getName() + " and " + indexColumns + " for " + indexName);
        tableDto.getTableVarColumns().addAll(tableVarColumns);
    }

    String getMethodName() {
        return _upperCasedMethodName;
    }

    private String addPrefix(String name, EditTranslationContext tContext) {
        return "Context." + tContext.getValidatorPrefix() + "_" + name;
    }

    public static boolean isInternalMethod(String methodName) {
        return _ALL_METHODS.contains(methodName.toUpperCase());
    }

    public static Set<String> getParsedUsedTables() {
        return _PARSED_USED_TABLES;
    }

    public static void resetParsedUsedTables() {
        _PARSED_USED_TABLES.clear();
    }
}
