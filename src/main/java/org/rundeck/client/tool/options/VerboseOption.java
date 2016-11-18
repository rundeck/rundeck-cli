package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 11/17/16.
 */
public interface VerboseOption {

    @Option(shortName = "v", longName = "verbose", description = "Extended verbose output")
    boolean isVerbose();

}
