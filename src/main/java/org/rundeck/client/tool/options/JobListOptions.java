package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 3/30/16.
 */
public interface JobListOptions extends JobBaseOptions{


    @Option(shortName = "j", longName = "job", description = "Job name filter")
    String getJob();

    boolean isJob();

    @Option(shortName = "g", longName = "group", description = "Job Group filter")
    String getGroup();

    boolean isGroup();

    @Option(shortName = "J", longName = "jobxact", description = "Exact Job name")
    String getJobExact();

    boolean isJobExact();

    @Option(shortName = "G", longName = "groupxact", description = "Exact Job Group")
    String getGroupExact();

    boolean isGroupExact();

    @Option(shortName = "i", longName = "idlist", description = "Comma separated list of Job IDs")
    String getIdlist();

    boolean isIdlist();

}
