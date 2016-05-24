package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 5/21/16.
 */
public interface RunOptions {

    @Option(shortName = "f", longName = "follow", description = "Follow execution output as it runs")
    boolean isFollow();
}
