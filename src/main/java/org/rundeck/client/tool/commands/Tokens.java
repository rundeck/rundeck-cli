package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ApiToken;
import org.rundeck.client.util.Client;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by greg on 8/9/16.
 */
@Command(description = "Create, and manage tokens")
public class Tokens extends ApiCommand {
    public Tokens(final HasClient client) {
        super(client);
    }

    public interface CreateOptions {
        @Option(longName = "user", shortName = "u", description = "user name")
        String getUser();
    }

    @Command(description = "Create a token for a user")
    public ApiToken create(CreateOptions options, CommandOutput output) throws IOException, InputError {
        ApiToken apiToken = apiCall(api -> api.createToken(options.getUser()));
        output.info("API Token created:");
        output.output(apiToken.getId());
        return apiToken;
    }

    public interface ListOptions {
        @Option(longName = "user", shortName = "u", description = "user name")
        String getUser();

        @Option(longName = "verbose", shortName = "v", description = "show full tokens")
        boolean isVerbose();
    }

    @Command(description = "List tokens for a user")
    public List<ApiToken> list(ListOptions options, CommandOutput output) throws IOException, InputError {
        List<ApiToken> tokens = apiCall(api -> api.listTokens(options.getUser()));
        output.info(String.format("API Tokens for %s:", options.getUser()));
        output.output(tokens.stream().map(
                options.isVerbose()
                ? ApiToken::getId
                : ApiToken::getTruncatedId
        ).collect(Collectors.toList()));

        return tokens;
    }

    public interface DeleteOptions {
        @Option(longName = "token", shortName = "t", description = "API token")
        String getToken();
    }

    @Command(description = "Delete a token")
    public boolean delete(DeleteOptions options, CommandOutput output) throws IOException, InputError {
        Void aVoid = apiCall(api -> api.deleteToken(options.getToken()));
        output.info("Token deleted.");
        return true;
    }
}
