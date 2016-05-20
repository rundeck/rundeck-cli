package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 5/20/16.
 */
public interface JobBaseOptions extends ProjectOptions {

    @Option(shortName = "f")
    File getFile();

    boolean isFile();

    @Option(shortName = "F", longName = "format", defaultValue = "xml", pattern = "^(xml|yaml)$")
    String getFormat();

}
