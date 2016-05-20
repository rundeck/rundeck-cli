package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 3/30/16.
 */
public interface JobListOptions extends BaseOptions{

    @Option(shortName = "p")
    String getProject();

    @Option(shortName = "j")
    String getJob();

    boolean isJob();

    @Option(shortName = "g")
    String getGroup();

    boolean isGroup();

    @Option(shortName = "i")
    String getIdlist();

    boolean isIdlist();

    @Option(shortName = "f")
    File getFile();

    boolean isFile();

    @Option(shortName = "F", longName = "format", defaultValue = "xml", pattern = "^(xml|yaml)$")
    String getFormat();
}
