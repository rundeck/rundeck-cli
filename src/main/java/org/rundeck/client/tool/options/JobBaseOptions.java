package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 5/20/16.
 */
public interface JobBaseOptions extends ProjectOptions {

    @Option(shortName = "f",
            longName = "file",
            description = "File path of the file to upload (load command) or destination for storing the jobs (list " +
                          "command)")
    File getFile();

    boolean isFile();

    @Option(shortName = "F",
            longName = "format",
            defaultValue = "xml",
            pattern = "^(xml|yaml)$",
            description = "Format for the Job definition file, either xml or yaml")
    String getFormat();


}
