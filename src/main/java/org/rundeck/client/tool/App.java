package org.rundeck.client.tool;

import com.simplifyops.toolbelt.*;
import com.simplifyops.toolbelt.format.json.jackson.JsonFormatter;
import com.simplifyops.toolbelt.format.yaml.snakeyaml.YamlFormatter;
import com.simplifyops.toolbelt.input.jewelcli.JewelInput;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.DateInfo;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.ScheduledJobItem;
import org.rundeck.client.tool.commands.*;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.ConfigSource;
import org.rundeck.client.util.Env;
import org.rundeck.client.util.ExtConfigSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;


/**
 * Entrypoint for commandline
 */
public class App {

    public static final String ENV_USER = "RD_USER";
    public static final String ENV_PASSWORD = "RD_PASSWORD";
    public static final String ENV_TOKEN = "RD_TOKEN";
    public static final String ENV_URL = "RD_URL";
    public static final String ENV_AUTH_PROMPT = "RD_AUTH_PROMPT";
    public static final String ENV_DEBUG = "RD_DEBUG";
    public static final String ENV_HTTP_TIMEOUT = "RD_HTTP_TIMEOUT";
    public static final String ENV_CONNECT_RETRY = "RD_CONNECT_RETRY";

    public static void main(String[] args) throws IOException, CommandRunFailure {
        tool("rd", new Config(new Env())).runMain(args, true);
    }


    private static void setupFormat(final ToolBelt belt, AppConfig config) {
        if ("yaml".equalsIgnoreCase(config.get("RD_FORMAT"))) {
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
            belt.formatter(new YamlFormatter(representer, dumperOptions));
            belt.channels().infoEnabled(false);
        } else if ("json".equalsIgnoreCase(config.get("RD_FORMAT"))) {
            belt.formatter(new JsonFormatter());
            belt.channels().infoEnabled(false);
        } else {
            NiceFormatter formatter = new NiceFormatter(null);
            formatter.setCollectionIndicator("");
            belt.formatter(formatter);
            belt.channels().info(new FormattedOutput(
                    belt.defaultOutput(),
                    new PrefixFormatter("# ", belt.defaultBaseFormatter())
            ));
        }
    }

    public static Tool tool(final String name, final Config config) {
        ToolBelt belt = ToolBelt.belt(name)
                                .defaultHelpCommands()
                                .ansiColorOutput(config.isAnsiEnabled())
                                .add(
                                        new Adhoc(config),
                                        new Jobs(config),
                                        new Projects(config),
                                        new Executions(config),
                                        new Run(config),
                                        new Keys(config),
                                        new RDSystem(config),
                                        new Scheduler(config),
                                        new Tokens(config),
                                        new Nodes(config),
                                        new Something()
                                )
                                .bannerResource("rd-banner.txt")
                                .commandInput(new JewelInput());
        setupColor(belt, config);
        setupFormat(belt, config);
        return belt.buckle();
    }

    public static interface AppConfig extends ConfigSource {
        boolean isAnsiEnabled();

        String getDateFormat();
    }

    static class Config extends ExtConfigSource implements HasClient, AppConfig {
        public Config(final ConfigSource src) {
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

        public String getDateFormat() {
            return getString("RD_DATE_FORMAT", "yyyy-MM-ddHH:mm:ssZ");
        }


        Client<RundeckApi> client;

        @Override
        public Client<RundeckApi> getClient() throws InputError {
            if (null == client) {
                client = App.createClient(this);
            }
            return client;
        }

        @Override
        public AppConfig getAppConfig() {
            return this;
        }
    }

    private static void setupColor(final ToolBelt belt, AppConfig config) {
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


    public static Client<RundeckApi> createClient(Config config) throws InputError {
        Auth auth = new Auth() {
        };
        String baseUrl = null;
        auth = auth.chain(new ConfigAuth(config));

        if (null == baseUrl) {
            baseUrl = config.require(
                    ENV_URL,
                    "Please specify the Rundeck base URL, e.g. http://host:port or http://host:port/api/14"
            );
        }

        if (config.getBool(ENV_AUTH_PROMPT, true) && null != System.console()) {
            auth = auth.chain(new ConsoleAuth(String.format("Credentials for URL: %s", baseUrl)).memoize());
        }

        int debuglevel = config.getInt(ENV_DEBUG, 0);
        Long httpTimeout = config.getLong(ENV_HTTP_TIMEOUT, null);
        Boolean retryConnect = config.getBool(ENV_CONNECT_RETRY, true);

        if (auth.isTokenAuth()) {
            return Rundeck.client(baseUrl, auth.getToken(), debuglevel, httpTimeout, retryConnect);
        } else {
            if (null == auth.getUsername() || "".equals(auth.getUsername().trim())) {
                throw new IllegalArgumentException("Username or token must be entered, or use environment variable " +
                                                   ENV_USER + " or " + ENV_TOKEN);
            }
            if (null == auth.getPassword() || "".equals(auth.getPassword().trim())) {
                throw new IllegalArgumentException("Password must be entered, or use environment variable " +
                                                   ENV_PASSWORD);
            }

            return Rundeck.client(
                    baseUrl,
                    auth.getUsername(),
                    auth.getPassword(),
                    debuglevel,
                    httpTimeout,
                    retryConnect
            );
        }
    }

    static interface Auth {
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
        Config config;

        public ConfigAuth(final Config config) {
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
        String header;
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
        Collection<Auth> chain;

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
        Auth auth;

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
        public void pond(CommandOutput out) {
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
}
