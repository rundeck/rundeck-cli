package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 11/17/16.
 */
public interface JobOutputFormatOption {

    @Option(shortName = "%",
            longName = "outformat",
            description = "Output format specifier for job data. You can use \"%key\" where key is one of:" +
                          "id, name, group, project, description, href, permalink, averageDuration. E.g. \"%id " +
                          "%href\". For 'jobs info' additional keys: scheduled, scheduleEnabled, enabled, scheduler" +
                          ".serverOwner, scheduler.serverNodeUUID.")
    String getOutputFormat();

    boolean isOutputFormat();
}
