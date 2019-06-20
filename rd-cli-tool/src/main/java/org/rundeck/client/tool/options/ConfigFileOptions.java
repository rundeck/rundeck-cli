package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.tool.commands.projects.Configure;

import java.io.File;

public interface ConfigFileOptions {
    @Option(shortName = "f",
            longName = "file",
            description = "Input file for project configuration. Can be a .properties, .json or .yaml file. " +
                          "Format is determined by file extension or -F/--format")
    File getFile();

    boolean isFile();

    @Option(shortName = "F",
            longName = "format",
            description = "Input file format. Can be [properties, json, yaml] (default: properties, unless " +
                          "recognized in filename)")
    Configure.InputFileFormat getFileFormat();

    boolean isFileFormat();
}
