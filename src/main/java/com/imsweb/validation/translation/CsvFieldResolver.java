package com.imsweb.validation.translation;

import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.validation.translation.metafile.MetafileField;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the FieldResolver that reads a comma-delimted file containing item number
 * and field name and uses that to supplement NAACCR and user dictionary fields when resolving
 * fields used in a EDITS metafile.
 * (note: does not conform to RFC 4180! quoting and escaping are not supported)
 */
@SuppressWarnings("unused")
public class CsvFieldResolver extends FieldResolver {
    private Map<Integer, String> _mappings;

    public CsvFieldResolver() {
        setMappings(new HashMap<>());
    }

    /**
     * Reads the comma delimited file into memory. If mappings are already in place,
     * new mappings are loaded over top of the old ones. This way, multiple mapping files
     * can be loaded.
     * @param mappingsFile
     * @throws IOException
     */
    public void loadMappingsFile(File mappingsFile) throws IOException {
        if (mappingsFile == null) {
            throw new IOException("Mappings file cannot be null");
        }
        if (! mappingsFile.exists()) {
            throw new IOException("Mappings file does not exist");
        }

        for (String line : Files.readAllLines(mappingsFile.toPath(), StandardCharsets.UTF_8)) {
            String[] mapping = line.trim().split("\\s*,\\s*");
            if (mapping.length != 2) {
               throw new IOException("Invalid line in mapping file: " + line);
            }
            getMappings().put(Integer.parseInt(mapping[0]), mapping[1]);
            System.out.println("      >> Loaded mapping from " + mappingsFile.getName() + ": " + mapping[0] + "->" + mapping[1]);
        }
    }

    @Override
    protected String resolveFieldPostDictionary(MetafileField field, TranslationConfiguration conf) {
        String propName = super.resolveFieldPostDictionary(field, conf);

        // if unable to resolve by superclass implementation, try the in memory mapping
        if (propName == null) {
            int itemNumber = field.getNumber();
            if (getMappings().containsKey(itemNumber)) {
                propName = getMappings().get(itemNumber);
            }
        }

        // if unable to resolve from the mapping file, create a (properly named) dummy item and warn the user
        if (propName == null) {
            propName = NaaccrXmlDictionaryUtils.createNaaccrIdFromItemName(field.getName());
            System.out.println("      >> Unsupported field: " + field.getName() + " (#" + field.getNumber() + "); deriving the ID from the name: " + propName);
        }
        return propName;
    }

    public Map<Integer, String> getMappings() {
        return _mappings;
    }

    public void setMappings(Map<Integer, String> mappings) {
        this._mappings = mappings;
    }

}
