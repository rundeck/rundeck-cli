package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * ID for execution
 */
public interface ExecutionIdOption {

    @Option(shortName = "e", longName = "eid", description = "Execution ID")
    String getId();
}
