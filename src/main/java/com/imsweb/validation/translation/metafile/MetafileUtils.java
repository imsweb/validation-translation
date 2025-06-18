/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.validation.translation.metafile;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MetafileUtils {

    // this class needs to use try-with-resources and use proper SQL parameters, but those are not trivial changes; for now I disabled the spotbug warnings

    public static Metafile extractMetafileInformation(File smf) {
        if (!smf.exists() || !smf.isFile())
            throw new RuntimeException("Unable to open '" + smf.getPath() + "'");

        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to open find drivers for SQLite");
        }

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + smf.getPath())) {

            // collect control information
            Map<String, String> controlInfo = new HashMap<>();
            Statement stmt = con.createStatement();
            ResultSet result = stmt.executeQuery("select * from EE_Control;");
            while (result.next())
                controlInfo.put(result.getString("ItemName"), result.getString("ItemValue"));
            result.close();
            stmt.close();

            // collect agencies
            Map<Integer, MetafileAgency> agencies = new HashMap<>();
            stmt = con.createStatement();
            result = stmt.executeQuery("select * from EE_Agencies;");
            while (result.next()) {
                MetafileAgency agency = new MetafileAgency();
                agency.setPrimaryKey(result.getInt("PKey"));
                agency.setAdminCode(result.getString("AdminCode"));
                agency.setOrganizationName(result.getString("OrgName"));
                agency.setLastModified(readDate(result.getString("LastModDate")));
                agency.setComments(result.getString("Comments"));
                agencies.put(agency.getPrimaryKey(), agency);
            }
            result.close();
            stmt.close();

            // collect messages
            Map<Integer, MetafileMessage> messages = new HashMap<>();
            stmt = con.createStatement();
            result = stmt.executeQuery("select * from EE_Messages;");
            while (result.next()) {
                MetafileMessage message = new MetafileMessage();
                message.setPrimaryKey(result.getInt("PKey"));
                message.setAgency(agencies.get(result.getInt("AgencyPKey")));
                message.setNumber(result.getInt("MessageNumber"));
                message.setMessage(result.getString("Message"));
                message.setLastModified(readDate(result.getString("LastModDate")));
                messages.put(message.getPrimaryKey(), message);
            }
            result.close();
            stmt.close();

            // collect fields
            Map<Integer, MetafileField> fields = new HashMap<>();
            stmt = con.createStatement();
            result = stmt.executeQuery("select * from EE_Fields;");
            while (result.next()) {
                MetafileField field = new MetafileField();
                field.setPrimaryKey(result.getInt("PKey"));
                field.setAgency(agencies.get(result.getInt("AgencyPKey")));
                field.setNumber(result.getInt("FieldNumber"));
                field.setName(result.getString("FieldName"));
                field.setLocalName(result.getString("LocalName"));
                field.setLength(result.getInt("FieldLength"));
                field.setType(result.getString("DataType"));
                field.setLastModified(readDate(result.getString("LastModDate")));
                field.setComments(result.getString("Comments"));
                fields.put(field.getPrimaryKey(), field);
            }
            result.close();
            stmt.close();

            // collect sets
            Map<Integer, MetafileSet> sets = new HashMap<>();
            stmt = con.createStatement();
            result = stmt.executeQuery("select * from EE_EditSets;");
            while (result.next()) {
                MetafileSet set = new MetafileSet();
                set.setPrimaryKey(result.getInt("PKey"));
                set.setAgency(agencies.get(result.getInt("AgencyPKey")));
                set.setTag(result.getString("EditSetTag"));
                set.setName(result.getString("EditSetName"));
                set.setLastModified(readDate(result.getString("LastModDate")));
                set.setDescription(result.getString("Description"));
                sets.put(set.getPrimaryKey(), set);
            }
            result.close();
            stmt.close();

            // collect tables
            Map<Integer, MetafileTable> tables = new HashMap<>();
            stmt = con.createStatement();
            result = stmt.executeQuery("select * from EE_LookupTables;");
            while (result.next()) {
                MetafileTable table = new MetafileTable();
                table.setPrimaryKey(result.getInt("PKey"));
                table.setAgency(agencies.get(result.getInt("AgencyPKey")));
                table.setName(result.getString("TableName").toUpperCase());
                table.setRecordCount(result.getInt("RecCount"));
                table.setLastModified(readDate(result.getString("LastModDate")));
                table.setComments(result.getString("Comments"));

                // collect the triggers for this table, this will tell us which columns are "fake" ones (triggered-maintained indexes)
                List<String> triggeredMaintainedIndexNames = new ArrayList<>();
                Pattern p = Pattern.compile("^CREATE TRIGGER update.+UPDATE " + table.getName() + "\\s+SET \\[(.+?)].+$", Pattern.MULTILINE | Pattern.DOTALL);
                Statement stmt2 = con.createStatement();
                ResultSet result2 = stmt2.executeQuery("select sql from sqlite_master where type = 'trigger' and tbl_name = '" + table.getName() + "';");
                while (result2.next()) {
                    String sql = result2.getString("sql");
                    Matcher m = p.matcher(sql);
                    if (m.matches())
                        triggeredMaintainedIndexNames.add(m.group(1));
                }
                result2.close();
                stmt2.close();
                table.setTriggeredMaintainedIndexes(triggeredMaintainedIndexNames);

                // collect table headers and content
                stmt2 = con.createStatement();
                result2 = stmt2.executeQuery("select * from " + table.getName() + ";");
                List<String> headers = new ArrayList<>();
                Map<String, MetafileColumnMetaData> columnsMetaData = new HashMap<>();
                for (int i = 2; i <= result2.getMetaData().getColumnCount(); i++) { // 1-based and ignore first PKey column
                    headers.add(result2.getMetaData().getColumnName(i).toUpperCase());
                    MetafileColumnMetaData data = new MetafileColumnMetaData();
                    data.setColumnName(result2.getMetaData().getColumnName(i).toUpperCase());
                    data.setColumnType(result2.getMetaData().getColumnTypeName(i));
                    // it looks like the columns size (the precision) is not properly returned for triggered-maintain columns (it's always set to 1); for now I deal with it
                    // in the translation, but a better approach would be to parse the trigger, gather the concatenated columns, and add their size...
                    data.setColumnSize(result2.getMetaData().getPrecision(i));
                    columnsMetaData.put(result2.getMetaData().getColumnName(i), data);
                    // make sure we expect the column type
                    if (!"CHAR".equals(result2.getMetaData().getColumnTypeName(i)) && !"VARCHAR".equals(result2.getMetaData().getColumnTypeName(i)) && !"INTEGER".equals(result2.getMetaData().getColumnTypeName(i)))
                        throw new RuntimeException("Unsupported type for table '" + table.getName() + ": " + result2.getMetaData().getColumnTypeName(i));
                }
                table.setHeaders(headers);
                table.setMetaData(columnsMetaData);

                // populate table content
                List<List<String>> content = new ArrayList<>();
                while (result2.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 2; i <= result2.getMetaData().getColumnCount(); i++) // 1-based and ignore first PKey column
                        row.add(result2.getString(i));
                    content.add(row);
                }
                table.setContent(content);
                result2.close();
                stmt2.close();

                // collect the single-column indexes
                Map<String, List<String>> singleColumnIndexes = new HashMap<>();
                p = Pattern.compile("^CREATE INDEX.+\\(\\[(.+?)].+$");
                stmt2 = con.createStatement();
                result2 = stmt2.executeQuery("select sql from sqlite_master where type = 'index' and tbl_name = '" + table.getName() + "';");
                while (result2.next()) {
                    Matcher m = p.matcher(result2.getString("sql"));
                    if (m.matches() && !triggeredMaintainedIndexNames.contains(m.group(1))) {
                        boolean allowDup = result2.getString("sql").contains("[PKey]");
                        int columnIdx = headers.indexOf(m.group(1).toUpperCase());
                        List<String> indexContent = new ArrayList<>();
                        for (List<String> row : content) {
                            String val = row.get(columnIdx);
                            if (allowDup || !indexContent.contains(val))
                                indexContent.add(val);
                        }
                        singleColumnIndexes.put(m.group(1), indexContent);
                    }
                }
                table.setSingleColumnIndexes(singleColumnIndexes);
                result2.close();
                stmt2.close();

                tables.put(table.getPrimaryKey(), table);
            }
            result.close();
            stmt.close();

            // collect edits
            Map<Integer, MetafileEdit> edits = new HashMap<>();
            stmt = con.createStatement();
            result = stmt.executeQuery("select * from EE_Edits;");
            while (result.next()) {
                MetafileEdit edit = new MetafileEdit();
                edit.setPrimaryKey(result.getInt("PKey"));
                edit.setAgency(agencies.get(result.getInt("AgencyPKey")));
                edit.setTag(result.getString("EditTag"));
                edit.setName(result.getString("EditName"));
                edit.setDefaultMessage(messages.get(result.getInt("MessagePKey")));

                // collect messages used by this edit
                Statement stmt2 = con.createStatement();
                ResultSet result2 = stmt2.executeQuery("select * from EE_EditMessagesPivot where EditPKey = " + edit.getPrimaryKey()+ ";");
                List<MetafileMessage> messagesForEdit = new ArrayList<>();
                while (result2.next())
                    messagesForEdit.add(messages.get(result2.getInt("MessagePKey")));
                edit.setMessages(messagesForEdit);
                result2.close();
                stmt2.close();

                // collect free error messages used by this edit
                stmt2 = con.createStatement();
                result2 = stmt2.executeQuery("select * from EE_EditErrorsPivot where EditPKey = " + edit.getPrimaryKey() + ";");
                List<String> errorsForEdit = new ArrayList<>();
                while (result2.next())
                    errorsForEdit.add(result2.getString("ErrorText"));
                edit.setErrorMessages(errorsForEdit);
                result2.close();
                stmt2.close();

                // collect the fields used by this edit
                stmt2 = con.createStatement();
                result2 = stmt2.executeQuery("select * from EE_EditFieldsPivot where EditPKey = " + edit.getPrimaryKey() + ";");
                Map<Integer, MetafileField> fieldsForEdit = new TreeMap<>();
                while (result2.next())
                    fieldsForEdit.put(result2.getInt("FieldOrder"), fields.get(result2.getInt("FieldPKey")));
                edit.setFields(fieldsForEdit);
                result2.close();
                stmt2.close();

                // collect the tables used by this edit
                stmt2 = con.createStatement();
                result2 = stmt2.executeQuery("select * from EE_EditTablesPivot where EditPKey = " + edit.getPrimaryKey() + ";");
                List<MetafileTable> tablesForEdit = new ArrayList<>();
                while (result2.next())
                    tablesForEdit.add(tables.get(result2.getInt("LookupTablePKey")));
                edit.setTables(tablesForEdit);
                result2.close();
                stmt2.close();

                // collect the sets using this edit
                stmt2 = con.createStatement();
                result2 = stmt2.executeQuery("select * from EE_EditSetEditsPivot where EditPKey = " + edit.getPrimaryKey() + ";");
                List<MetafileSet> setsForEdit = new ArrayList<>();
                while (result2.next())
                    setsForEdit.add(sets.get(result2.getInt("EditSetPKey")));
                edit.setSets(setsForEdit);
                result2.close();
                stmt2.close();

                edit.setLastModified(readDate(result.getString("LastModDate")));
                edit.setDescription(result.getString("Description"));
                edit.setLogic(result.getString("EditLogic").replaceAll("\\t", "    ").replaceAll("\r\n", "\n").replaceAll("(^\\s+|^\\n+|\\s+$|\\n+$)", ""));
                edit.setAdminNotes(result.getString("AdminNotes"));
                edits.put(edit.getPrimaryKey(), edit);
            }
            result.close();
            stmt.close();

            Metafile metafile = new Metafile();
            metafile.setName(smf.getName());
            metafile.setStructureVersion(controlInfo.get("MFStructureVersion"));
            metafile.setContentVersion(controlInfo.get("MFContentVersion"));
            metafile.setComment(controlInfo.get("MFComment"));
            metafile.setAgencies(new ArrayList<>(agencies.values()));
            metafile.setMessages(new ArrayList<>(messages.values()));
            metafile.setFields(new ArrayList<>(fields.values()));
            metafile.setEdits(new ArrayList<>(edits.values()));
            metafile.setSets(new ArrayList<>(sets.values()));
            metafile.setTables(new ArrayList<>(tables.values()));
            return metafile;

        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static LocalDateTime readDate(String value) {
        if (value == null || value.isEmpty())
            return null;

        if (value.matches("\\d{8}"))
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();

        if (value.matches("\\d{8} \\d{2}:\\d{2}\\d{2}"))
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));

        return null;
    }

    // this replaces the F1, F2, etc... and V1, V2, etc... by the field names and/or values
    public static String fixMessage(String msg, MetafileEdit edit, boolean fixDc) {
        List<Integer> fieldIndexes = edit.getFields().keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        for (int fieldIdx : fieldIndexes) {
            MetafileField field = edit.getFields().get(fieldIdx);
            if (field.getPropertyName() == null)
                throw new RuntimeException("Trying to reference a missing property name for field " + field.getName());
            String valReference = "${untrimmedline." + field.getPropertyName() + ("D1".equalsIgnoreCase(field.getType()) ? ".formatDate()" : "") + "}";
            msg = msg.replace("%F" + fieldIdx, field.getName()).replace("%V" + fieldIdx, valReference);
        }

        // %DC is dynamically replaced by an type of invalid message (future date, invalid year, etc...) when the message is set in the logic
        if (fixDc)
            msg = msg.replace("%DC", "invalid");

        return msg;
    }
}
