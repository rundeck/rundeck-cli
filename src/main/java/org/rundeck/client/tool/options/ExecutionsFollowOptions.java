package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 5/20/16.
 */
public interface ExecutionsFollowOptions extends BaseOptions {

    @Option(shortName = "e", longName = "eid")
    String getId();

    @Option(shortName = "q", longName = "quiet")
    boolean isQuiet();

    @Option(shortName = "r", longName = "progress")
    boolean isProgress();

    @Option(shortName = "t", longName = "restart")
    boolean isRestart();

    @Option(shortName = "T", longName = "tail", defaultValue = {"1"})
    long getTail();

    boolean isTail();
}
