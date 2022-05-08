package org.rundeck.client.tool.options;

import lombok.Data;
import picocli.CommandLine;

@Data
public class TokenFormatOption extends VerboseOption implements OutputFormat {
    @CommandLine.Option(names = {"-%", "--outformat"},
            description = "Output format specifier for Token info. You can use \"%%key\" where key is one of: " +
                    "token, id, user, creator, roles, expiration, expired. E.g. \"%%id:%%token\"")
    private String outputFormat;

}
