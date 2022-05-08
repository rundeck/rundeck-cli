package org.rundeck.client.tool.options;

import lombok.Data;
import picocli.CommandLine;

/**
 * @author greg
 * @since 10/20/17
 */
@Data
public class UserFormatOption extends VerboseOption implements OutputFormat {

    @CommandLine.Option(names = {"-%", "--outformat"},
            description = "Output format specifier for User info. You can use \"%%key\" where key is one of:" +
                    "login, firstName, lastName, email. E.g. \"%%login:%%email\"")
    String outputFormat;

}
