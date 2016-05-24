package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 5/20/16.
 */

public interface ExecutionsOptions extends OptionalProjectOptions {

    @Option(shortName = "m", longName = "max", description = "Maximum number of results to retrieve at once.")
    int getMax();

    boolean isMax();

    @Option(shortName = "o", longName = "offset", description = "First result offset to receive.")
    int getOffset();

    boolean isOffset();

    @Option(shortName = "e", longName = "eid", defaultToNull = true, description = "Execution ID")
    String getId();

}
