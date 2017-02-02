package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * @author greg
 * @since 2/2/17
 */
public interface ProjectListFormatOptions {

    @Option(shortName = "%",
            longName = "outformat",
            description = "Output format specifier for project info. You can use \"%key\" where key is one of: " +
                          "name, description, url, config, config.KEY. E.g. \"%name: " +
                          "%description\".")
    String getOutputFormat();

    boolean isOutputFormat();
}
