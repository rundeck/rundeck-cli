package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * @author greg
 * @since 11/22/16
 */
public interface NodeOutputFormatOption {

    @Option(shortName = "%",
            longName = "outformat",
            description = "Output format specifier for Node info. You can use \"%key\" where key is one of:" +
                          "nodename, hostname, osFamily, osVersion, osArch, description, username, tags, or any " +
                          "attribute. E.g. \"%nodename %tags\"")
    String getOutputFormat();

    boolean isOutputFormat();
}
