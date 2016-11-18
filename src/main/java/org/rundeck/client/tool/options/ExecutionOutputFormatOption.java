package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 11/17/16.
 */
public interface ExecutionOutputFormatOption {

    @Option(shortName = "%",
            longName = "outformat",
            description = "Output format specifier for execution data. You can use \"%key\" where key is one of:" +
                          "id, project, description, argstring, permalink, href, status, job, user, serverUUID, " +
                          "dateStarted, dateEnded, successfulNodes, failedNodes. E.g. \"%id %href\"")
    String getOutputFormat();

    boolean isOutputFormat();
}
