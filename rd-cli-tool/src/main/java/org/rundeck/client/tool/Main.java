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

package org.rundeck.client.tool;

import org.rundeck.client.RundeckClient;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.DateInfo;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.scheduler.ScheduledJobItem;
import org.rundeck.client.tool.commands.*;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.util.AdaptedToolbeltOutput;
import org.rundeck.client.tool.util.ExtensionLoaderUtil;
import org.rundeck.client.util.*;
import org.rundeck.toolbelt.*;
import org.rundeck.toolbelt.format.json.jackson.JsonFormatter;
import org.rundeck.toolbelt.format.yaml.snakeyaml.YamlFormatter;
import org.rundeck.toolbelt.input.jewelcli.JewelInput;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;

import static org.rundeck.client.RundeckClient.ENV_INSECURE_SSL;
import static org.rundeck.client.RundeckClient.ENV_INSECURE_SSL_NO_WARN;


/**
 * Entrypoint for commandline
 */
public class Main {

    public static final String ENV_USER          = "RD_USER";
    public static final String ENV_PASSWORD      = "RD_PASSWORD";
    public static final String ENV_TOKEN         = "RD_TOKEN";
    public static final String ENV_URL           = "RD_URL";
    public static final String ENV_API_VERSION   = "RD_API_VERSION";
    public static final String ENV_AUTH_PROMPT   = "RD_AUTH_PROMPT";
    public static final String ENV_DEBUG         = "RD_DEBUG";
    public static final String ENV_RD_FORMAT     = "RD_FORMAT";
    public static final String
            USER_AGENT =
            RundeckClient.Builder.getUserAgent("rd-cli-tool/" + org.rundeck.client.Version.VERSION);

    public static void main(String[] args) throws CommandRunFailure {
        Rd rd = new Rd(new Env());
        Tool tool = tool(rd);
        boolean success = false;
        try {
            success = tool.runMain(args, false);
        } catch (RequestFailed failure) {
            rd.getOutput().error(failure.getMessage());
            if (rd.getDebugLevel() > 0) {
                StringWriter sb = new StringWriter();
                failure.printStackTrace(new PrintWriter(sb));
                rd.getOutput().error(sb.toString());
            }
        }
        if (!success) {
            System.exit(2);
        }
    }

    private static void setupFormat(final ToolBelt belt, RdClientConfig config) {
        final String format = config.get(ENV_RD_FORMAT);
        if ("yaml".equalsIgnoreCase(format)) {
            configYamlFormat(belt, config);
        } else if ("json".equalsIgnoreCase(format)) {
            configJsonFormat(belt);
        } else {
            if (null != format) {
                belt.finalOutput().warning(String.format("# WARNING: Unknown value for %s: %s", ENV_RD_FORMAT, format));
            }
            configNiceFormat(belt);
        }
    }

    private static void configNiceFormat(final ToolBelt belt) {
        NiceFormatter formatter = new NiceFormatter(null) {
            @Override
            public String format(final Object o) {
                if (o instanceof DataOutput) {
                    DataOutput o1 = (DataOutput) o;
                    Map<?, ?> map = o1.asMap();
                    if (null != map) {
                        return super.format(map);
                    }
                    List<?> objects = o1.asList();
                    if (null != objects) {
                        return super.format(objects);
                    }
                }
                return super.format(o);
            }
        };
        formatter.setCollectionIndicator("");
        belt.formatter(formatter);
        belt.channels().info(new FormattedOutput(
                belt.defaultOutput(),
                new PrefixFormatter("# ", belt.defaultBaseFormatter())
        ));
    }

    private static void configJsonFormat(final ToolBelt belt) {
        belt.formatter(new JsonFormatter(DataOutputAsFormatable));
        belt.channels().infoEnabled(false);
        belt.channels().warningEnabled(false);
        belt.channels().errorEnabled(false);
    }

    private static void configYamlFormat(final ToolBelt belt, final RdClientConfig config) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(
                "BLOCK".equalsIgnoreCase(config.getString("RD_YAML_FLOW", "BLOCK")) ?
                DumperOptions.FlowStyle.BLOCK :
                DumperOptions.FlowStyle.FLOW
        );
        dumperOptions.setPrettyFlow(config.getBool("RD_YAML_PRETTY", true));
        Representer representer = new Representer();
        representer.addClassTag(JobItem.class, Tag.MAP);
        representer.addClassTag(ScheduledJobItem.class, Tag.MAP);
        representer.addClassTag(DateInfo.class, Tag.MAP);
        representer.addClassTag(Execution.class, Tag.MAP);
        belt.formatter(new YamlFormatter(DataOutputAsFormatable, new Yaml(representer, dumperOptions)));
        belt.channels().infoEnabled(false);
        belt.channels().warningEnabled(false);
        belt.channels().errorEnabled(false);
    }

    private static final Function<Object, Optional<Formatable>> DataOutputAsFormatable = o -> {
        if (o instanceof DataOutput) {
            return Optional.of(new Formatable() {
                @Override
                public List<?> asList() {
                    return ((DataOutput) o).asList();
                }

                @Override
                public Map<?, ?> asMap() {
                    return ((DataOutput) o).asMap();
                }
            });
        }
        return Optional.empty();
    };

    public static Tool tool(final Rd rd) {
        List<Object> base = new ArrayList<>(Arrays.asList(
                new Adhoc(rd),
                new Jobs(rd),
                new Projects(rd),
                new Executions(rd),
                new Run(rd),
                new Keys(rd),
                new RDSystem(rd),
                new Scheduler(rd),
                new Tokens(rd),
                new Nodes(rd),
                new Users(rd),
                new Something(),
                new Retry(rd),
                new Metrics(rd),
                new Version()
        ));
        AppCommand commandTool = new AppCommand(rd);
        List<RdCommandExtension> extensions = ExtensionLoaderUtil.list();
        extensions.forEach(ext -> ext.setRdTool(commandTool));
        base.addAll(extensions);

        ToolBelt belt = ToolBelt.belt("rd")
                                .defaultHelpCommands()
                                .ansiColorOutput(rd.isAnsiEnabled())
                                .add(base.toArray())
                                .bannerResource("rd-banner.txt",Collections.singletonMap("@version@",org.rundeck.client.Version.VERSION))
                                .commandInput(new JewelInput());

        belt.printStackTrace(rd.getDebugLevel() > 0);
        setupColor(belt, rd);
        setupFormat(belt, rd);

        boolean insecureSsl = Boolean.parseBoolean(System.getProperty(
                "rundeck.client.insecure.ssl",
                System.getenv(ENV_INSECURE_SSL)
        ));
        boolean insecureSslNoWarn = Boolean.parseBoolean(System.getenv(ENV_INSECURE_SSL_NO_WARN));
        if (insecureSsl && !insecureSslNoWarn ) {
            belt.finalOutput().warning(
                    "# WARNING: RD_INSECURE_SSL=true, no hostname or certificate trust verification will be performed");
        }
        belt.handles(InputError.class,  (err, context) -> {
            context.getOutput().warning(String.format(
                    "Input error for [%s]: %s",
                    context.getCommandsString(),
                    err.getMessage()
            ));
            context.getOutput().warning(String.format(
                    "You can use: \"%s %s\" to get help.",
                    context.getCommandsString(),
                    "-h"
            ));
            return true;
        });
        rd.setOutput(new AdaptedToolbeltOutput(belt.finalOutput()));

        if (rd.getDebugLevel() > 0) {
            extensions.forEach(ext -> {
                rd.getOutput().warning("# Including extension: " + ext.getClass().getName());
            });
        }

        return belt.buckle();
    }

    static class Rd extends ExtConfigSource implements RdApp, RdClientConfig {
        Client<RundeckApi> client;
        private CommandOutput output;

        public Rd(final ConfigSource src) {
            super(src);
        }

        public boolean isAnsiEnabled() {
            String term = getString("TERM", null);
            String rd_color = getString("RD_COLOR", null);
            return "1".equals(rd_color) ||
                   (
                           term != null
                           && term.contains("color")
                           && !"0".equals(rd_color)
                   );
        }

        @Override
        public int getDebugLevel() {
            return getInt(ENV_DEBUG, 0);
        }

        public String getDateFormat() {
            return getString("RD_DATE_FORMAT", "yyyy-MM-dd'T'HH:mm:ssXX");
        }

        @Override
        public Client<RundeckApi> getClient() throws InputError {
            if (null == client) {
                try {
                    client = Main.createClient(this);
                } catch (ConfigSourceError configSourceError) {
                    throw new InputError(configSourceError.getMessage());
                }
            }
            return client;
        }

        @Override
        public Client<RundeckApi> getClient(final int version) throws InputError {
            try {
                client = Main.createClient(this, version);
            } catch (ConfigSourceError configSourceError) {
                throw new InputError(configSourceError.getMessage());
            }
            return client;
        }

        @Override
        public <T> ServiceClient<T> getClient(final Class<T> api, final int version) throws InputError {
            try {
                return Main.createClient(this, api, version);
            } catch (ConfigSourceError configSourceError) {
                throw new InputError(configSourceError.getMessage());
            }
        }

        @Override
        public <T> ServiceClient<T> getClient(final Class<T> api) throws InputError {
            try {
                return Main.createClient(this, api, null);
            } catch (ConfigSourceError configSourceError) {
                throw new InputError(configSourceError.getMessage());
            }
        }

        @Override
        public RdClientConfig getAppConfig() {
            return this;
        }

        public void versionDowngradeWarning(int requested, int supported) {
            getOutput().warning(String.format(
                    "# WARNING: API Version Downgraded: %d -> %d",
                    requested,
                    supported
            ));
            getOutput().warning(String.format(
                    "# WARNING: To avoid this warning, specify the API version via RD_URL: " +
                    "export RD_URL=%sapi/%s",
                    client.getAppBaseUrl(),
                    supported
            ));
            getOutput().warning("# WARNING: To disable downgrading: " +
                                "export RD_API_DOWNGRADE=false");
        }

        public CommandOutput getOutput() {
            return output;
        }

        public void setOutput(CommandOutput output) {
            this.output = output;
        }
    }

    private static void setupColor(final ToolBelt belt, RdClientConfig config) {
        if (config.isAnsiEnabled()) {
            String info = config.get("RD_COLOR_INFO");
            if (null != info) {
                belt.ansiColor().info(info);
            }
            String output = config.get("RD_COLOR_OUTPUT");
            if (null != output) {
                belt.ansiColor().output(output);
            }
            String warn = config.get("RD_COLOR_WARN");
            if (null != warn) {
                belt.ansiColor().warning(warn);
            }
            String error = config.get("RD_COLOR_ERROR");
            if (null != error) {
                belt.ansiColor().error(error);
            }
        }
    }


    public static Client<RundeckApi> createClient(Rd config) throws ConfigSource.ConfigSourceError {
        return createClient(config, RundeckApi.class, null);
    }

    public static <T> Client<T> createClient(Rd config, Class<T> api) throws ConfigSource.ConfigSourceError {
        return createClient(config, api, null);
    }

    public static Client<RundeckApi> createClient(Rd config, Integer requestedVersion)
            throws ConfigSource.ConfigSourceError
    {
        return createClient(config, RundeckApi.class, requestedVersion);
    }

    public static <T> Client<T> createClient(Rd config, Class<T> api, Integer requestedVersion)
            throws ConfigSource.ConfigSourceError
    {

        Auth auth = new Auth() {
        };
        auth = auth.chain(new ConfigAuth(config));
        String baseUrl = config.require(
                ENV_URL,
                "Please specify the Rundeck base URL, e.g. http://host:port or http://host:port/api/14"
        );

        if (!auth.isConfigured() && config.getBool(ENV_AUTH_PROMPT, true) && null != System.console()) {
            auth = auth.chain(new ConsoleAuth(String.format("Credentials for URL: %s", baseUrl)).memoize());
        }
        RundeckClient.Builder<T> builder = RundeckClient.builder(api)
                                                        .baseUrl(baseUrl)
                                                        .config(config);
        if (null != requestedVersion) {
            builder.apiVersion(requestedVersion);
        } else {
            int anInt = config.getInt(ENV_API_VERSION, -1);
            if (anInt > 0) {
                builder.apiVersion(anInt);
            }
        }

        if (auth.isTokenAuth()) {
            builder.tokenAuth(auth.getToken());
        } else {
            if (null == auth.getUsername() || "".equals(auth.getUsername().trim())) {
                throw new IllegalArgumentException("Username or token must be entered, or use environment variable " +
                                                   ENV_USER + " or " + ENV_TOKEN);
            }
            if (null == auth.getPassword() || "".equals(auth.getPassword().trim())) {
                throw new IllegalArgumentException("Password must be entered, or use environment variable " +
                                                   ENV_PASSWORD);
            }
            builder.passwordAuth(auth.getUsername(), auth.getPassword());
        }
        builder.logger(new OutputLogger(config.getOutput()));
        builder.userAgent("rd-cli-tool/" + org.rundeck.client.Version.VERSION);
        return builder.build();

    }

    interface Auth {
        default boolean isConfigured() {
            return null != getToken() || (
                    null != getUsername() && null != getPassword()
            );
        }

        default String getUsername() {
            return null;
        }

        default String getPassword() {
            return null;
        }

        default String getToken() {
            return null;
        }

        default boolean isTokenAuth() {
            String username = getUsername();
            if (null != username && !"".equals(username.trim())) {
                return false;
            }
            String token = getToken();
            return null != token && !"".equals(token);
        }

        default Auth chain(Auth auth) {
            return new ChainAuth(Arrays.asList(this, auth));
        }

        default Auth memoize() {
            return new MemoAuth(this);
        }
    }


    static class ConfigAuth implements Auth {
        final ConfigSource config;

        public ConfigAuth(final ConfigSource config) {
            this.config = config;
        }

        @Override
        public String getUsername() {
            return config.get(ENV_USER);
        }

        @Override
        public String getPassword() {
            return config.get(ENV_PASSWORD);
        }

        @Override
        public String getToken() {
            return config.get(ENV_TOKEN);
        }
    }

    static class ConsoleAuth implements Auth {
        String username;
        String pass;
        String token;
        final String header;
        boolean echoHeader;

        public ConsoleAuth(final String header) {
            this.header = header;
            echoHeader = false;
        }

        @Override
        public String getUsername() {
            echo();
            return System.console().readLine("Enter username (blank for token auth): ");
        }

        private void echo() {
            if (!echoHeader) {
                if (null != header) {
                    System.out.println(header);
                }
                echoHeader = true;
            }
        }

        @Override
        public String getPassword() {
            echo();
            char[] chars = System.console().readPassword("Enter password: ");
            return new String(chars);
        }

        @Override
        public String getToken() {
            echo();
            char[] chars = System.console().readPassword("Enter auth token: ");
            return new String(chars);
        }
    }

    static class ChainAuth implements Auth {
        final Collection<Auth> chain;

        public ChainAuth(final Collection<Auth> chain) {
            this.chain = chain;
        }

        @Override
        public String getUsername() {
            return findFirst(Auth::getUsername);
        }

        private String findFirst(Function<Auth, String> func) {
            for (Auth auth : chain) {
                String user = func.apply(auth);
                if (null != user) {
                    return user;
                }
            }
            return null;
        }

        @Override
        public String getPassword() {
            return findFirst(Auth::getPassword);
        }

        @Override
        public String getToken() {
            return findFirst(Auth::getToken);
        }
    }


    static class MemoAuth implements Auth {
        final Auth auth;

        public MemoAuth(final Auth auth) {
            this.auth = auth;
        }

        String username;
        boolean usermemo = false;
        String pass;
        boolean passmemo = false;
        String token;
        boolean tokenmemo = false;

        @Override
        public String getUsername() {
            if (usermemo) {
                return username;
            }
            username = auth.getUsername();
            usermemo = true;
            return username;
        }

        @Override
        public String getPassword() {
            if (passmemo) {
                return pass;
            }
            pass = auth.getPassword();
            passmemo = true;
            return pass;
        }

        @Override
        public String getToken() {
            if (tokenmemo) {
                return token;
            }
            token = auth.getToken();
            tokenmemo = true;
            return token;
        }
    }

    @Hidden
    @Command("pond")
    public static class Something {
        @Command
        public void pond(org.rundeck.toolbelt.CommandOutput out) {
            int i = new Random().nextInt(4);
            ANSIColorOutput.ColorString kind;
            switch (i) {
                case 1:
                    kind = ANSIColorOutput.colorize(ANSIColorOutput.Color.BLUE, "A little luck.");
                    break;
                case 2:
                    kind = ANSIColorOutput.colorize(ANSIColorOutput.Color.GREEN, "Good luck.");
                    break;
                case 3:
                    kind = ANSIColorOutput.colorize(ANSIColorOutput.Color.ORANGE, "Great luck.");
                    break;
                default:
                    kind = ANSIColorOutput.colorize(ANSIColorOutput.Color.RESET, "Big trouble.");
                    break;
            }

            out.output("For your reference, today you will have:");
            out.output(kind);
        }
    }

    private static class OutputLogger implements Client.Logger {
        final CommandOutput output;

        public OutputLogger(final org.rundeck.toolbelt.CommandOutput output) {
            this.output = new AdaptedToolbeltOutput(output);
        }
        public OutputLogger(final CommandOutput output) {
            this.output = output;
        }

        @Override
        public void output(final String out) {
            output.output(out);
        }

        @Override
        public void warning(final String warn) {
            output.warning(warn);
        }

        @Override
        public void error(final String err) {
            output.error(err);
        }
    }
}
