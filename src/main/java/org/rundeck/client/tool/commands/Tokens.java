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
import org.rundeck.client.tool.options.TokenFormatOption;
import org.rundeck.client.util.Format;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;
import org.rundeck.client.api.model.ApiToken;
import org.rundeck.client.api.model.CreateToken;
import org.rundeck.client.tool.RdApp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    public interface CreateOptions extends TokenFormatOption {
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
        boolean v19 = getClient().minApiVersion(19);
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
        } else {
            if (options.isRoles() || options.isDuration()) {
                throw new InputError("--roles/-r and --duration/-d are not supported for API v18 and earlier");
            }
            apiToken = apiCall(api -> api.createToken(options.getUser()));
        }
        output.info("API Token created:");

        output.output(
                formatTokenOutput(v19, options, ApiToken::toMap, true)
                        .apply(apiToken)
        );

        return apiToken;
    }

    public interface ListOptions extends TokenFormatOption {
        @Option(longName = "user", shortName = "u", description = "user name")
        String getUser();

        @Option(longName = "verbose", shortName = "v", description = "show full tokens")
        boolean isVerbose();
    }

    @Command(description = "List tokens for a user")
    public List<ApiToken> list(ListOptions options, CommandOutput output) throws IOException, InputError {

        List<ApiToken> tokens = apiCall(api -> api.listTokens(options.getUser()));
        output.info(String.format("API Tokens for %s:", options.getUser()));

        output.output(
                tokens.stream()
                      .map(
                              formatTokenOutput(
                                      getClient().minApiVersion(19),
                                      options,
                                      ApiToken::toMap,
                                      false
                              )
                      )
                      .collect(Collectors.toList())
        );

        return tokens;
    }

    /**
     * Formatter for displaying output tokens
     *
     * @param toMap function to convert to map
     * @param reveal
     * @param v19     true if v19 or later
     * @return formatter for token output
     */
    private Function<? super ApiToken, ?> formatTokenOutput(
            final boolean v19,
            TokenFormatOption options,
            Function<ApiToken, Map<?, ?>> toMap,
            final boolean reveal
    )
    {
        if (options.isOutputFormat()) {
            return Format.formatter(options.getOutputFormat(), toMap, "%", "");
        } else if (options.isVerbose()) {
            return toMap;
        } else if (v19) {
            return reveal ? ApiToken::getToken : ApiToken::getId;
        } else {
            return reveal ? ApiToken::getIdOrToken : ApiToken::getTruncatedIdOrToken;
        }
    }

    public interface RevealOption extends TokenFormatOption {
        @Option(longName = "id", shortName = "id", description = "Token ID")
        String getId();
    }

    @Command(description = "Reveal token value for an ID (API v19+)")
    public void reveal(RevealOption options, CommandOutput output) throws IOException, InputError {
        ApiToken token = apiCall(api -> api.getToken(options.getId()));
        output.info(String.format("API Token %s:", options.getId()));
        output.output(
                formatTokenOutput(true, options, ApiToken::toMap, true)
                        .apply(token)
        );
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
