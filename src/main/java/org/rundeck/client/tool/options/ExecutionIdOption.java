package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 6/6/16.
 */
public interface ExecutionIdOption {

    @Option(shortName = "e", longName = "eid", description = "Execution ID")
    String getId();
}
