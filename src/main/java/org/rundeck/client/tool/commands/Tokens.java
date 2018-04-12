/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.Option;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;
import org.rundeck.client.api.model.ApiToken;
import org.rundeck.client.api.model.CreateToken;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.VerboseOption;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * tokens subcommands
 */
@Command(description = "Create, and manage tokens")
public class Tokens extends AppCommand {
    public Tokens(final RdApp client) {
        super(client);
    }

    public interface CreateOptions extends VerboseOption {
        @Option(longName = "user", shortName = "u", description = "user name")
        String getUser();

        @Option(longName = "duration",
                shortName = "d",
                description = "Token duration, in the form '#[ydhms]`, e.g. '2y','5h','24h30m'")
        String getDuration();

        boolean isDuration();

        @Option(longName = "roles",
                shortName = "r",
                description = "List of roles to set for the token, space separated (api v19+)")
        List<String> getRoles();

        boolean isRoles();
    }

    @Command(description = "Create a token for a user")
    public ApiToken create(CreateOptions options, CommandOutput output) throws IOException, InputError {
        boolean v19 = getClient().getApiVersion() >= 19;
        ApiToken apiToken;
        if (v19) {
            if (!options.isRoles()) {
                throw new InputError("--roles/-r is required for API v19");
            }
            apiToken = apiCall(api -> api.createToken(new CreateToken(
                    options.getUser(),
                    options.getRoles(),
                    options.getDuration()
            )));
            output.info("API Token created:");
            output.output(options.isVerbose() ? apiToken.toMap() : apiToken.getToken());
        } else {
            if (options.isRoles() || options.isDuration()) {
                throw new InputError("--roles/-r and --duration/-d are not supported for API v18 and earlier");
            }
            apiToken = apiCall(api -> api.createToken(options.getUser()));
            output.info("API Token created:");
            if (options.isVerbose()) {
                output.output(apiToken.toMap());
            } else {
                output.output(apiToken.getIdOrToken());
            }
        }
        return apiToken;
    }

    public interface ListOptions extends VerboseOption {
        @Option(longName = "user", shortName = "u", description = "user name")
        String getUser();

        @Option(longName = "verbose", shortName = "v", description = "show full tokens")
        boolean isVerbose();
    }

    @Command(description = "List tokens for a user")
    public List<ApiToken> list(ListOptions options, CommandOutput output) throws IOException, InputError {

        List<ApiToken> tokens = apiCall(api -> api.listTokens(options.getUser()));
        output.info(String.format("API Tokens for %s:", options.getUser()));

        output.output(tokens.stream()
                            .map(
                                    formatTokenOutput(
                                            getClient().getApiVersion() >= 19,
                                            options.isVerbose()
                                    )
                            )
                            .collect(Collectors.toList()));

        return tokens;
    }

    /**
     * Formatter for displaying output tokens
     *
     * @param v19     true if v19 or later
     * @param verbose true for verbose
     *
     * @return formatter for token output
     */
    private Function<? super ApiToken, ?> formatTokenOutput(
            final boolean v19, final boolean verbose
    )
    {
        Function<? super ApiToken, ?> v19Output = verbose
                                                  ? ApiToken::toMap
                                                  : ApiToken::getId;
        Function<? super ApiToken, ?> v18Output = verbose
                                                  ? ApiToken::getIdOrToken
                                                  : ApiToken::getTruncatedIdOrToken;
        return v19 ? v19Output : v18Output;
    }

    public interface RevealOption extends VerboseOption {
        @Option(longName = "id", shortName = "id", description = "Token ID")
        String getId();
    }

    @Command(description = "Reveal token value for an ID (API v19+)")
    public void reveal(RevealOption options, CommandOutput output) throws IOException, InputError {
        ApiToken token = apiCall(api -> api.getToken(options.getId()));
        output.info(String.format("API Token %s:", options.getId()));
        output.output(options.isVerbose() ? token.toMap() : token.getToken());
    }

    public interface DeleteOptions {
        @Option(longName = "token", shortName = "t", description = "API token")
        String getToken();
    }

    @Command(description = "Delete a token")
    public void delete(DeleteOptions options, CommandOutput output) throws IOException, InputError {
        Void aVoid = apiCall(api -> api.deleteToken(options.getToken()));
        output.info("Token deleted.");
    }
}
