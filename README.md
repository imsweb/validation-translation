# Java Metafile Translation

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation-translation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation-translation)

This project allows a Genedits Metafile to be translated into Groovy edits that can be executed in the SEER Validation framework.

## Usage

There are three ways to execute a translation:

1. Translate a given metafile into a Validator object that can then be loaded into the SEER Validation Engine.
2. Execute a "full" translation of a metafile which involves creating the XML edits file and optionally the pre-compiled source code for the translated edits.
3. Translate a single edit in a given metafile (this is mainly used to investigate issues in the translation).

The three methods are demonstrated in corresponding "Demo" classes in the test package.

The full translation is the recommended way to use the library; once a new version of the metafile is released, copy it into a new folder and set that 
folder as the working directory in the translation configuration. If a previous version of the metafile was already translated, also set the previous 
working folder so the output of the previous translation can be used as input of the new one (this is used to make sure edits keep the same ID).

There are other configuration parameters to set; refer to the documentation of the TranslationConfiguration class or the demo classes.

The full translation also allows to create the Groovy source code for loading the edits as pre-compiled edits in the validation engine,  
which is much faster than loading the XML file. A typical usage for that advanced feature is to maintain the source code of the translated edit into 
a different project where they can be compiled, put into a JAR and released like a normal library.

## Full Translation

The full translation requires a folder containing the metafile to translate. It will then create an "output" folder containing the following:

 1. The XML file with the translated edits.
 2. That same file compressed.
 3. A file with edit mapping IDs that can be used as input of a future translation.
 4. A file with set mapping IDs that can be used as input of a future translation.
 5. A properties file with the basic validator properties (version and such).

A "src" folder will also be created if the pre-compiled source code is being generated.

Here is an example of running a full translation:

```java
TranslationConfiguration conf = new TranslationConfiguration();
conf.setWorkingDirectoryPath("path_to_folder_containing_metafile");
conf.setPreviousWorkingDirectoryPath("path_to_previous_translation_folder"); // optional
conf.setMetafileName("metafile_to_translate.smf");
conf.setTranslationPrefix("DEMO");
conf.setNaaccrVersion("220");

new MetafileTranslator().executeFullTranslation(conf);
```

## Command Line
The compiled JAR can be executed from the command line to perform a full translation.

**Command Line Options:**
- `-p`: The working folder containing the metafile
- `-c`: The file name of the configuration properties file within the working folder

**Example usage:**
```sh
$ java -jar validation-translation.jar -c config.properties -p WORKING_DIR
```
For an example of a translation configuration, see `examples/config.properties`

## Pre-Compiled Groovy Edits
For faster loading of large metafiles, the optionally generated Groovy source code can be pre-compiled into bytecode. The folder `examples/compile-groovy` 
contains an example of a build.gradle and settings.gradle that can be placed in the output folder to generate a JAR containing compiled bytecode.

## About SEER

This library was developed through the [SEER](http://seer.cancer.gov/) program.

The Surveillance, Epidemiology and End Results program is a premier source for cancer statistics in the United States.
The SEER program collects information on incidence, prevalence and survival from specific geographic areas representing
a large portion of the US population and reports on all these data plus cancer mortality data for the entire country.
