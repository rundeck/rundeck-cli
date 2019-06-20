package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * @author greg
 * @since 10/20/17
 */
public interface UserFormatOption extends VerboseOption {

    @Option(shortName = "%",
            longName = "outformat",
            description = "Output format specifier for User info. You can use \"%key\" where key is one of:" +
                          "login, firstName, lastName, email. E.g. \"%login:%email\"")
    String getOutputFormat();

    boolean isOutputFormat();
}
