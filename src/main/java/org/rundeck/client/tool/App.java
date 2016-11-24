package org.rundeck.client.tool;

import com.simplifyops.toolbelt.*;
import com.simplifyops.toolbelt.format.json.jackson.JsonFormatter;
import com.simplifyops.toolbelt.format.yaml.snakeyaml.YamlFormatter;
import com.simplifyops.toolbelt.input.jewelcli.JewelInput;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.commands.*;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Env;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
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
        tool("rd").runMain(args, true);
    }


    private static void setupFormat(final ToolBelt belt) {
        if ("yaml".equalsIgnoreCase(Env.getString("RD_FORMAT", null))) {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(
                    "BLOCK".equalsIgnoreCase(Env.getString("RD_YAML_FLOW", "BLOCK")) ?
                    DumperOptions.FlowStyle.BLOCK :
                    DumperOptions.FlowStyle.FLOW
            );
            dumperOptions.setPrettyFlow(Env.getBool("RD_YAML_PRETTY", true));
            Representer representer = new Representer();
            representer.addClassTag(JobItem.class, Tag.MAP);
            representer.addClassTag(ScheduledJobItem.class, Tag.MAP);
            representer.addClassTag(DateInfo.class, Tag.MAP);
            representer.addClassTag(Execution.class, Tag.MAP);
            belt.formatter(new YamlFormatter(representer, dumperOptions));
            belt.channels().infoEnabled(false);
        } else if ("json".equalsIgnoreCase(Env.getString("RD_FORMAT", null))) {
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

    public static Tool tool(final String name) {
        ToolBelt belt = ToolBelt.belt(name)
                                .defaultHelpCommands()
                                .ansiColorOutput(isAnsiEnabled())
                                .add(
                                        new Adhoc(App::createClient),
                                        new Jobs(App::createClient),
                                        new Projects(App::createClient),
                                        new Executions(App::createClient),
                                        new Run(App::createClient),
                                        new Keys(App::createClient),
                                        new RDSystem(App::createClient),
                                        new Scheduler(App::createClient),
                                        new Tokens(App::createClient),
                                        new Nodes(App::createClient)
                                )
                                .bannerResource("rd-banner.txt")
                                .commandInput(new JewelInput());
        setupColor(belt);
        setupFormat(belt);
        return belt.buckle();
    }

    private static void setupColor(final ToolBelt belt) {
        if (isAnsiEnabled()) {
            String info = System.getenv("RD_COLOR_INFO");
            if (null != info) {
                belt.ansiColor().info(info);
            }
            String output = System.getenv("RD_COLOR_OUTPUT");
            if (null != output) {
                belt.ansiColor().output(output);
            }
            String warn = System.getenv("RD_COLOR_WARN");
            if (null != warn) {
                belt.ansiColor().warning(warn);
            }
            String error = System.getenv("RD_COLOR_ERROR");
            if (null != error) {
                belt.ansiColor().error(error);
            }
        }
    }

    private static boolean isAnsiEnabled() {
        return "1".equals(System.getenv("RD_COLOR")) ||
               (
                       System.getenv("TERM") != null
                       && System.getenv("TERM").contains("color")
                       && !"0".equals(System.getenv("RD_COLOR"))
               );
    }

    public static Client<RundeckApi> createClient() {
        Auth auth = new Auth() {
        };
        String baseUrl = null;
        auth = auth.chain(new EnvAuth());

        if (null == baseUrl) {
            baseUrl = Env.require(
                    ENV_URL,
                    "Please specify the Rundeck base URL, e.g. http://host:port or http://host:port/api/14"
            );
        }

        if (Env.getBool(ENV_AUTH_PROMPT, true) && null != System.console()) {
            auth = auth.chain(new ConsoleAuth(String.format("Credentials for URL: %s", baseUrl)).memoize());
        }

        int debuglevel = Env.getInt(ENV_DEBUG, 0);
        Long httpTimeout = Env.getLong(ENV_HTTP_TIMEOUT, null);
        Boolean retryConnect = Env.getBool(ENV_CONNECT_RETRY, true);

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


    static class EnvAuth implements Auth {
        @Override
        public String getUsername() {
            return System.getenv(ENV_USER);
        }

        @Override
        public String getPassword() {
            return System.getenv(ENV_PASSWORD);
        }

        @Override
        public String getToken() {
            return System.getenv(ENV_TOKEN);
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

    /**
     * Use credentials from framework.properties if set
     */
    static class RdeckBaseAuth implements Auth {
        File rdeckBase;
        Properties properties;

        public RdeckBaseAuth(final File rdeckBase) {
            this.rdeckBase = rdeckBase;
        }

        public String getBaseUrl() {
            return loadProps().getProperty("framework.server.url");
        }

        @Override
        public String getUsername() {
            return loadProps().getProperty("framework.server.username");
        }

        private Properties loadProps() {
            if (properties != null) {
                return properties;
            }
            properties = new Properties();
            try {
                try (FileInputStream fis = new FileInputStream(new File(rdeckBase, "etc/framework.properties"))) {
                    properties.load(fis);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return properties;
        }

        @Override
        public String getPassword() {
            return loadProps().getProperty("framework.server.password");
        }

        @Override
        public String getToken() {
            return null;
        }

        static boolean isAvailable() {
            return Env.getString("RDECK_BASE", null) != null
                   && Env.getBool("RD_USE_RDECK_BASE", false);
        }

        static RdeckBaseAuth get() {
            return new RdeckBaseAuth(new File(Env.getString("RDECK_BASE", null)));
        }
    }

}
