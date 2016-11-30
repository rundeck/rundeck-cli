package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * @author greg
 * @since 11/30/16
 */
public interface NodeFilterOptions {

    @Option(shortName = "F", longName = "filter", description = "A node filter string")
    String getFilter();

    boolean isFilter();

}
