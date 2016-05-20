package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 3/30/16.
 */
public interface JobListOptions extends JobBaseOptions{


    @Option(shortName = "j")
    String getJob();

    boolean isJob();

    @Option(shortName = "g")
    String getGroup();

    boolean isGroup();

    @Option(shortName = "i")
    String getIdlist();

    boolean isIdlist();

}
