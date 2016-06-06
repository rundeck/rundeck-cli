package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;

/**
 * Options for listing executions
 */
public interface ExecutionListOptions {

    @Option(shortName = "m", longName = "max", description = "Maximum number of results to retrieve at once.")
    int getMax();

    boolean isMax();

    @Option(shortName = "o", longName = "offset", description = "First result offset to receive.")
    int getOffset();

    boolean isOffset();


}
