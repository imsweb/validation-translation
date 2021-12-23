/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.translation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.imsweb.validation.ValidationXmlUtils;

public class PreviousTranslationInfo {

    // previous edits mappings
    private final Map<String, TranslationMapping> _previousEditsMappings;

    // previous sets mappings
    private final Map<String, TranslationMapping> _previousSetsMappings;

    // previous validator properties
    private final Properties _previousTranslationProperties;

    public PreviousTranslationInfo(TranslationConfiguration conf) throws TranslationException {
        File previousWorkingDir = conf.getPreviousWorkingDirectoryPath() == null ? null : new File(conf.getPreviousWorkingDirectoryPath(), conf.getOutputDirectoryName());

        File previousEditsFile = new File(previousWorkingDir, conf.getEditMappingsFilename());
        if (previousEditsFile.exists())
            _previousEditsMappings = readMappings(previousEditsFile);
        else
            _previousEditsMappings = new HashMap<>();

        File previousSetsFile = new File(previousWorkingDir, conf.getSetMappingsFilename());
        if (previousSetsFile.exists())
            _previousSetsMappings = readMappings(previousSetsFile);
        else
            _previousSetsMappings = new HashMap<>();

        File previousValidatorProperties = new File(previousWorkingDir, conf.getValidatorPropertiesFilename());
        _previousTranslationProperties = new Properties();
        if (previousValidatorProperties.exists()) {
            try {
                try (FileInputStream is = new FileInputStream(previousValidatorProperties)) {
                    _previousTranslationProperties.load(is);
                }
            }
            catch (IOException e) {
                throw new TranslationException("Unable to load " + previousValidatorProperties.getPath(), e);
            }
        }
    }

    public Set<String> getAllEditsTagInPreviousMapping() {
        return _previousEditsMappings.keySet();
    }

    // name is only needed while we have to stay compatible with old file formats (so no tags); it should be removed eventually...
    public TranslationMapping getPreviousMappingForEditTag(String tag, String name) {
        TranslationMapping prevEdit = _previousEditsMappings.get(tag);

        if (prevEdit == null && !_previousEditsMappings.isEmpty() && _previousEditsMappings.values().iterator().next().getTag() == null)
            prevEdit = _previousEditsMappings.values().stream().filter(e -> name.equals(e.getName())).findAny().orElse(null);

        return prevEdit;
    }

    public Map<String, TranslationMapping> getAllPreviousEditsMapping() {
        return _previousEditsMappings;
    }

    public Set<String> getAllSetsTagInPreviousMapping() {
        return _previousSetsMappings.keySet();
    }

    // name and all names are only needed while we have to stay compatible with old file formats (so no tags); they should be removed eventually...
    public TranslationMapping getPreviousMappingForSetTag(String tag) {
        TranslationMapping result = _previousSetsMappings.get(tag);

        // if no result was found, see if one was manually overridden in the previous file
        if (result == null)
            result = _previousSetsMappings.values().stream().filter(m -> tag.equals(m.getForcedTag())).findFirst().orElse(null);

        return result;
    }

    public Map<String, TranslationMapping> getAllPreviousSetsMapping() {
        return _previousSetsMappings;
    }

    public Map<String, TranslationMapping> readMappings(File file) throws TranslationException {
        Map<String, TranslationMapping> result = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.US_ASCII))) {
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = StringUtils.splitPreserveAllTokens(line.trim(), '|');

                    // new format for the files uses tags, but for now I still need to support the old one which is based on names
                    TranslationMapping dto = new TranslationMapping();
                    dto.setTag(parts[0]);
                    dto.setName(parts[1]);
                    dto.setId(parts[2]);
                    if (parts.length > 3)
                        dto.setHash(parts[3]);
                    if (parts.length > 4)
                        dto.setForcedTag(parts[4]);
                    result.put(dto.getTag(), dto);
                }

                line = reader.readLine();
            }
        }
        catch (IOException e) {
            throw new TranslationException("Unable to read " + file.getPath(), e);
        }

        return result;
    }

    public void writeMappings(Map<String, TranslationMapping> mappings, File file) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.US_ASCII))) {
            Set<String> dejaVu = new HashSet<>();
            mappings.entrySet().stream().sorted(Entry.comparingByKey()).forEach(entry -> {
                if (dejaVu.contains(entry.getValue().getId()))
                    throw new RuntimeException("Trying to write duplicate ID " + entry.getValue().getId());
                dejaVu.add(entry.getValue().getId());
                try {
                    writer.write(entry.getValue().getTag() + "|" + entry.getValue().getName() + "|" + entry.getValue().getId() + "|" + Objects.toString(entry.getValue().getHash(), "") + "\n");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TranslationMapping> extractIdentical(Map<String, TranslationMapping> previous, Map<String, TranslationMapping> current) {
        List<TranslationMapping> result = new ArrayList<>();

        // keys are edit tags
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(previous.keySet());
        allKeys.addAll(current.keySet());
        for (String key : allKeys) {
            TranslationMapping prev = previous.get(key), curr = current.get(key);
            if (prev != null && curr != null && prev.getHash() != null && prev.getHash().equals(curr.getHash()))
                result.add(curr);
        }

        return result;
    }

    public List<TranslationMapping> extractModified(Map<String, TranslationMapping> previous, Map<String, TranslationMapping> current) {
        List<TranslationMapping> result = new ArrayList<>();

        // keys are edit tags
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(previous.keySet());
        allKeys.addAll(current.keySet());
        for (String key : allKeys) {
            TranslationMapping prev = previous.get(key), curr = current.get(key);
            if (prev != null && curr != null && prev.getHash() != null && !prev.getHash().equals(curr.getHash()))
                result.add(curr);
        }

        return result;
    }

    public List<TranslationMapping> extractAdded(Map<String, TranslationMapping> previous, Map<String, TranslationMapping> current) {
        List<TranslationMapping> result = new ArrayList<>();

        // keys are edit tags
        for (TranslationMapping curr : current.values())
            if (!previous.containsKey(curr.getTag()))
                result.add(curr);

        return result;
    }

    public List<TranslationMapping> extractRemoved(Map<String, TranslationMapping> previous, Map<String, TranslationMapping> current) {
        List<TranslationMapping> result = new ArrayList<>();

        // keys are edit tags
        for (TranslationMapping prev : previous.values())
            if (!current.containsKey(prev.getTag()))
                result.add(prev);

        return result;
    }

    public String getPreviousAttribute(String key) {
        return _previousTranslationProperties.getProperty(key);
    }

    public String computeVersion(TranslationConfiguration conf) throws TranslationException {
        String previousVersion = _previousTranslationProperties.getProperty(ValidationXmlUtils.ROOT_ATTR_VERSION);
        if (previousVersion == null)
            return conf.getTranslationPrefix() + "-001-01";

        String[] parts = StringUtils.split(previousVersion, '-');
        String prefix = parts[0], majorVersion = parts[1], minorVersion = parts[2];

        if (!conf.getTranslationPrefix().equals(prefix))
            throw new TranslationException("Incompatible prefix " + prefix + " vs " + conf.getTranslationPrefix());

        // if the previous file translated was the same, then increase the minor version, otherwise increase the major one
        String currentName = conf.getMetafileName();
        String previousName = _previousTranslationProperties.getProperty(ValidationXmlUtils.ROOT_ATTR_TRANSLATED_FROM);
        boolean isPatch = currentName.substring(0, currentName.indexOf('.')).equals(previousName.substring(0, previousName.indexOf('.'))) && currentName.contains("patch");
        if (currentName.equals(previousName) || isPatch || conf.isMinorVersion())
            minorVersion = StringUtils.leftPad(String.valueOf(Integer.parseInt(minorVersion) + 1), 2, '0');
        else {
            majorVersion = StringUtils.leftPad(String.valueOf(Integer.parseInt(majorVersion) + 1), 3, '0');
            minorVersion = "01";
        }

        return prefix + "-" + majorVersion + "-" + minorVersion;
    }
}
