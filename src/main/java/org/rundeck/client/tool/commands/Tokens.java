package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ApiToken;
import org.rundeck.client.util.Client;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by greg on 8/9/16.
 */
@Command(description = "Create, and manage tokens")
public class Tokens extends ApiCommand {
    public Tokens(final Client<RundeckApi> client) {
        super(client);
    }

    public static interface CreateOptions {
        @Option(longName = "user", shortName = "u", description = "user name")
        String getUser();
    }

    @Command(description = "Create a token for a user")
    public ApiToken create(CreateOptions options, CommandOutput output) throws IOException {
        ApiToken apiToken = client.checkError(client.getService().createToken(options.getUser()));
        output.output("API Token created:");
        output.output(apiToken.getId());
        return apiToken;
    }

    public static interface ListOptions {
        @Option(longName = "user", shortName = "u", description = "user name")
        String getUser();

        @Option(longName = "verbose", shortName = "v", description = "show full tokens")
        boolean isVerbose();
    }

    @Command(description = "List tokens for a user")
    public List<ApiToken> list(ListOptions options, CommandOutput output) throws IOException {
        List<ApiToken> tokens = client.checkError(client.getService().listTokens(options.getUser()));
        output.output(String.format("API Tokens for %s:", options.getUser()));
        output.output(tokens.stream().map(
                options.isVerbose()
                ? ApiToken::getId
                : ApiToken::getTruncatedId
        ).collect(Collectors.toList()));

        return tokens;
    }

    public static interface DeleteOptions {
        @Option(longName = "token", shortName = "t", description = "API token")
        String getToken();
    }

    @Command(description = "Delete a token")
    public boolean delete(DeleteOptions options, CommandOutput output) throws IOException {
        Void aVoid = client.checkError(client.getService().deleteToken(options.getToken()));
        return true;
    }
}
