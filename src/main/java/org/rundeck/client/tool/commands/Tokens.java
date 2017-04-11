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
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.model.ApiToken;
import org.rundeck.client.tool.RdApp;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * tokens subcommands
 */
@Command(description = "Create, and manage tokens")
public class Tokens extends AppCommand {
    public Tokens(final RdApp client) {
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
    public void delete(DeleteOptions options, CommandOutput output) throws IOException, InputError {
        Void aVoid = apiCall(api -> api.deleteToken(options.getToken()));
        output.info("Token deleted.");
    }
}
