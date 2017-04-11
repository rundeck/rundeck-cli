package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * @author greg
 * @since 3/2/17
 */
public interface PagingResultOptions {

    @Option(shortName = "m", longName = "max", description = "Maximum number of results to retrieve at once.")
    int getMax();

    boolean isMax();

    @Option(shortName = "o", longName = "offset", description = "First result offset to receive.")
    int getOffset();

    boolean isOffset();
}
