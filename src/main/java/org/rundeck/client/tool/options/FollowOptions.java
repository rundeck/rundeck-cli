package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 5/21/16.
 */
public interface FollowOptions extends RunOptions{

    @Option(shortName = "q", longName = "quiet", description = "Echo no output, just wait until the execution completes.")
    boolean isQuiet();

    @Option(shortName = "r",
            longName = "progress",
            description = "Do not echo log text, just an indicator that output is being received.")
    boolean isProgress();

    @Option(shortName = "t", longName = "restart", description = "Restart from the beginning")
    boolean isRestart();

    @Option(shortName = "T",
            longName = "tail",
            defaultValue = {"1"},
            description = "Number of lines to tail from the end, default: 1")
    long getTail();

    boolean isTail();
}
