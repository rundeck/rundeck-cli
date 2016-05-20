package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 5/20/16.
 */
public interface ExecutionsOptions extends OptionalProjectOptions {

    @Option(shortName = "m", longName = "max")
    int getMax();

    boolean isMax();

    @Option(shortName = "o", longName = "offset")
    int getOffset();

    boolean isOffset();

    @Option(shortName = "e", longName = "eid", defaultToNull = true)
    String getId();

}
