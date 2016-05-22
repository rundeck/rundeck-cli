package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 5/20/16.
 */
public interface ExecutionsFollowOptions extends BaseOptions, FollowOptions {

    @Option(shortName = "e", longName = "eid")
    String getId();

}
