package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 3/30/16.
 */
public interface JobListOptions extends JobBaseOptions{


    @Option(shortName = "j", longName = "job", description = "Job name")
    String getJob();

    boolean isJob();

    @Option(shortName = "g", longName = "group", description = "Job Group")
    String getGroup();

    boolean isGroup();

    @Option(shortName = "i", longName = "idlist", description = "Comma separated list of Job IDs")
    String getIdlist();

    boolean isIdlist();

}
