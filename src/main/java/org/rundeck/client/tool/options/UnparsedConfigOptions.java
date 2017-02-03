package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Unparsed;

import java.util.List;

/**
 * @author greg
 * @since 2/2/17
 */
public interface UnparsedConfigOptions {

    @Unparsed(name = "-- --configkey=value",
              defaultValue = {},
              description = "A set of config properties for the project, in the form --key=value")
    List<String> config();
}
