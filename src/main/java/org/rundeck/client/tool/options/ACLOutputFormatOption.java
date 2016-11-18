package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 11/18/16.
 */
public interface ACLOutputFormatOption {

    @Option(shortName = "%",
            longName = "outformat",
            description = "Output format specifier for ACL info. You can use \"%key\" where key is one of:" +
                          "name, type, href. E.g. \"%name %href\"")
    String getOutputFormat();

    boolean isOutputFormat();
}
