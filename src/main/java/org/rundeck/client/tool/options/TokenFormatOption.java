package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

public interface TokenFormatOption extends VerboseOption {
    @Option(shortName = "%",
            longName = "outformat",
            description = "Output format specifier for Token info. You can use \"%key\" where key is one of: " +
                    "token, id, user, creator, roles, expiration, expired. E.g. \"%id:%token\"")
    String getOutputFormat();

    boolean isOutputFormat();
}
