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

import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.OutputFormat;
import org.rundeck.client.tool.options.VerboseOption;
import picocli.CommandLine;
import org.rundeck.client.tool.options.TokenFormatOption;
import org.rundeck.client.util.Format;


import org.rundeck.client.tool.InputError;
import org.rundeck.client.api.model.ApiToken;
import org.rundeck.client.api.model.CreateToken;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * tokens subcommands
 */
@CommandLine.Command(description = "Create, and manage tokens", name = "tokens")
public class Tokens extends BaseCommand {


    @Getter @Setter
    static class CreateOptions extends TokenFormatOption {
        @CommandLine.Option(names = {"--user", "-u"}, description = "user name", required = true)
        String user;

        @CommandLine.Option(names = {"--duration", "-d"},
                description = "Token duration, in the form '#[ydhms]`, e.g. '2y','5h','24h30m'")
        String duration;

        boolean isDuration() {
            return duration != null;
        }

        @CommandLine.Option(names = {"--roles", "-r"},
                arity = "1..*",
                description = "List of roles to set for the token, comma separated (api v41+)")
        List<String> roles;

        boolean isRoles() {
            return roles != null && !roles.isEmpty();
        }
    }

    @CommandLine.Command(description = "Create a token for a user")
    public ApiToken create(@CommandLine.Mixin CreateOptions options) throws IOException, InputError {
        boolean v19 = getRdTool().getClient().minApiVersion(19);
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
        getRdOutput().info(String.format("API Token created: %s", apiToken.getId()));

        getRdOutput().output(
                formatTokenOutput(v19, options, ApiToken::toMap, true)
                        .apply(apiToken)
        );

        return apiToken;
    }

    @Getter @Setter
    static class ListOptions extends TokenFormatOption {
        @CommandLine.Option(names = {"--user", "-u"}, description = "user name", required = true)
        private String user;

    }

    @CommandLine.Command(description = "List tokens for a user")
    public List<ApiToken> list(@CommandLine.Mixin ListOptions options) throws IOException, InputError {

        List<ApiToken> tokens = apiCall(api -> api.listTokens(options.getUser()));
        getRdOutput().info(String.format("API Tokens for %s:", options.getUser()));

        getRdOutput().output(
                tokens.stream()
                        .map(
                                formatTokenOutput(
                                        getRdTool().getClient().minApiVersion(19),
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
     * @param reveal true to show token value
     * @param v19     true if v19 or later
     * @return formatter for token output
     */
    private Function<? super ApiToken, ?> formatTokenOutput(
            final boolean v19,
            OutputFormat options,
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

    @Getter
    @Setter
    static class RevealOption extends VerboseOption implements OutputFormat {
        @CommandLine.Option(names = {"--id", "-i"}, description = "Token ID", required = true)
        private String id;

        @CommandLine.Option(names = {"-%", "--outformat"},
                description = "Output format specifier for Token info. You can use \"%%key\" where key is one of: " +
                        "token, id, user, creator, roles, expiration, expired. E.g. \"%%id:%%token\"")
        private String outputFormat;
    }

    @CommandLine.Command(description = "Reveal token value for an ID (API v19+) [@|red DEPRECATED|@: use @|bold rd tokens info|@]", hidden = true)
    public void reveal(@CommandLine.Mixin RevealOption options) throws IOException, InputError {
        getRdOutput().info("@|faint rd tokens reveal is deprecated, use: rd tokens info|@");
        getInfo(options, true);
    }

    @CommandLine.Command(description = "Get token info for an ID (API v19+)")
    public void info(@CommandLine.Mixin RevealOption options) throws IOException, InputError {
        getInfo(options, false);
    }

    private void getInfo(@CommandLine.Mixin RevealOption options, boolean reveal) throws IOException, InputError {
        ApiToken token = apiCall(api -> api.getToken(options.getId()));
        getRdOutput().info(String.format("API Token %s:", options.getId()));
        getRdOutput().output(
                formatTokenOutput(true, options, ApiToken::toMap, reveal)
                        .apply(token)
        );
    }

    @Getter
    @Setter
    static class DeleteOptions {
        @CommandLine.Option(names = {"--token", "-t"}, description = "API token [deprecated, use --id]", hidden = true)
        private String token;

        @CommandLine.Option(names = {"--id", "-i"}, description = "Token ID")
        private String id;
    }

    @CommandLine.Command(description = "Delete a token")
    public void delete(@CommandLine.Mixin DeleteOptions options) throws IOException, InputError {
        String tokenId = options.getId();
        if (tokenId == null) {
            tokenId = options.getToken();
            if (tokenId != null) {
                getRdOutput().warning("--token is deprecated use --id");
            }
        }
        if (tokenId == null) {
            throw new InputError("Missing required option: '--id=<id>'");
        }
        final String id = tokenId;
        Void aVoid = apiCall(api -> api.deleteToken(id));
        getRdOutput().info("Token deleted.");
    }
}
