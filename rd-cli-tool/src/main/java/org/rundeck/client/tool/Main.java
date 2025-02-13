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

import org.jetbrains.annotations.NotNull;
import org.rundeck.client.RundeckClient;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.DateInfo;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.scheduler.ScheduledJobItem;
import org.rundeck.client.tool.commands.*;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.tool.format.*;
import org.rundeck.client.tool.output.SystemOutput;
import org.rundeck.client.tool.util.ExtensionLoaderUtil;
import org.rundeck.client.tool.util.Resources;
import org.rundeck.client.util.*;
import org.rundeck.client.util.DataOutput;
import picocli.CommandLine;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.rundeck.client.RundeckClient.*;


/**
 * Entrypoint for commandline
 */
@CommandLine.Command(
        name = "rd",
        version = org.rundeck.client.Version.VERSION,
        mixinStandardHelpOptions = true,
        subcommands = {
                Adhoc.class,
                Auth.class,
                Jobs.class,
                Projects.class,
                Executions.class,
                Run.class,
                Keys.class,
                RDSystem.class,
                Scheduler.class,
                Tokens.class,
                Nodes.class,
                Users.class,
                Main.Something.class,
                Retry.class,
                Metrics.class,
                Version.class
        }
)
public class Main {
    public static final String RD_USER = "RD_USER";
    public static final String RD_PASSWORD = "RD_PASSWORD";
    public static final String RD_TOKEN = "RD_TOKEN";
    public static final String RD_AUTH = "RD_AUTH";
    public static final String RD_URL = "RD_URL";
    public static final String RD_API_VERSION = "RD_API_VERSION";
    public static final String RD_AUTH_PROMPT = "RD_AUTH_PROMPT";
    public static final String RD_DEBUG = "RD_DEBUG";
    public static final String RD_FORMAT = "RD_FORMAT";
    public static final String RD_EXT_DISABLED = "RD_EXT_DISABLED";
    public static final String RD_EXT_DIR = "RD_EXT_DIR";

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;
    public static final String
            USER_AGENT =
            RundeckClient.Builder.getUserAgent("rd-cli-tool/" + org.rundeck.client.Version.VERSION);

    public static void main(String[] args) {
        int result = -1;
        try (Rd rd = createRd()) {
            RdToolImpl rd1 = new RdToolImpl(rd);
            CommandLine commandLine = new CommandLine(new Main(), new CmdFactory(rd1));
            CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme.Builder(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO))
                    .commands(CommandLine.Help.Ansi.Style.fg_white)
                    .applySystemProperties() // optional: allow end users to customize
                    .build();
            commandLine.setColorScheme(colorScheme);
            commandLine.setExpandAtFiles(false);
            commandLine.setUsageHelpAutoWidth(true);
            commandLine.setHelpFactory(new CommandLine.IHelpFactory() {
                @Override
                public CommandLine.Help create(CommandLine.Model.CommandSpec commandSpec, CommandLine.Help.ColorScheme colorScheme) {
                    return new CommandLine.Help(commandSpec, colorScheme) {
                        /**
                         * Returns a sorted map of the subcommands.
                         */
                        @Override
                        public Map<String, CommandLine.Help> subcommands() {
                            return new TreeMap<>(super.subcommands());
                        }

                        @Override
                        public String commandListHeading(Object... params) {
                            return "\nAvailable commands:\n\n";
                        }
                    };
                }
            });

            commandLine.getHelpSectionMap().put(
                    CommandLine.Model.UsageMessageSpec.SECTION_KEY_HEADER_HEADING,
                    help -> loadBanner("rd-banner.txt", Collections.singletonMap("$version$", org.rundeck.client.Version.VERSION))
            );
            commandLine.setExecutionExceptionHandler((Exception ex, CommandLine cl, CommandLine.ParseResult parseResult) -> {
                if (ex instanceof InputError) {
                    return cl.getParameterExceptionHandler().handleParseException(
                            new CommandLine.ParameterException(cl, ex.getMessage(), ex),
                            args
                    );
                }
                if (ex instanceof RequestFailed) {
                    rd.getOutput().error(ex.getMessage());
                    if (rd.getDebugLevel() > 0) {
                        StringWriter sb = new StringWriter();
                        ex.printStackTrace(new PrintWriter(sb));
                        rd.getOutput().error(sb.toString());
                    }
                    return 2;
                }
                throw ex;
            });

            loadCommands(rd, rd1).forEach(commandLine::addSubcommand);

            result = commandLine.execute(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(result);
    }

    @NotNull
    private static Rd createRd() {
        ConfigSource config = buildConfig();
        loadExtensionJars(config);
        RdBuilder builder = new RdBuilder();
        Rd rd = new Rd(config);
        setup(rd, builder);
        return rd;
    }

    static String loadBanner(String resource, Map<String, String> replacements) {
        InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream(resource);
        if (null != resourceAsStream) {
            try {
                String result;
                try (BufferedReader is = new BufferedReader(new InputStreamReader(resourceAsStream))) {
                    result = is.lines().collect(Collectors.joining("\n"));
                }
                if (replacements != null && !replacements.isEmpty()) {
                    for (String s : replacements.keySet()) {
                        String val = replacements.get(s);
                        result = result.replaceAll(Pattern.quote(s), Matcher.quoteReplacement(val));
                    }
                }
                return CommandLine.Help.Ansi.AUTO.string(result);
            } catch (IOException ignored) {

            }
        }
        return null;
    }

    private static ConfigSource buildConfig() {
        return new ConfigBase(new MultiConfigValues(new Env(), new SysProps()));
    }

    private static void loadExtensionJars(ConfigSource config) {
        if (config.getBool(RD_EXT_DISABLED, false)) {
            return;
        }
        String rd_ext_dir = config.get(RD_EXT_DIR);
        if(null==rd_ext_dir){
            return;
        }
        File extDir = new File(rd_ext_dir);
        if (!extDir.isDirectory()) {
            return;
        }
        File[] jars = extDir.listFiles(f -> f.getName().endsWith(".jar"));
        //add to class loader
        if(jars==null){
            return;
        }
        URLClassLoader urlClassLoader = buildClassLoader(jars);
        Thread.currentThread().setContextClassLoader(urlClassLoader);
    }

    private static URLClassLoader buildClassLoader(final File[] jars) {
        ClassLoader parent = Main.class.getClassLoader();
        final List<URL> urls = new ArrayList<>();
        try {
            for (File jar : jars) {
                final URL url = jar.toURI().toURL();
                urls.add(url);
            }
            return URLClassLoader.newInstance(urls.toArray(new URL[0]), parent);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating classloader for urls: " + urls, e);
        }
    }

    private static void setupFormat(final RdBuilder belt, RdClientConfig config) {
        final String format = config.get(RD_FORMAT);
        if ("yaml".equalsIgnoreCase(format)) {
            configYamlFormat(belt, config);
        } else if ("json".equalsIgnoreCase(format)) {
            configJsonFormat(belt);
        } else {
            if (null != format) {
                belt.finalOutput().warning(String.format("# WARNING: Unknown value for %s: %s", RD_FORMAT, format));
            }
            configNiceFormat(belt);
        }
    }

    private static void configNiceFormat(final RdBuilder belt) {
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

    private static void configJsonFormat(final RdBuilder belt) {
        belt.formatter(new JsonFormatter(DataOutputAsFormatable));
        belt.channels().infoEnabled(false);
        belt.channels().warningEnabled(false);
        belt.channels().errorEnabled(false);
    }

    private static void configYamlFormat(final RdBuilder belt, final RdClientConfig config) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(
                "BLOCK".equalsIgnoreCase(config.getString("RD_YAML_FLOW", "BLOCK")) ?
                        DumperOptions.FlowStyle.BLOCK :
                        DumperOptions.FlowStyle.FLOW
        );
        dumperOptions.setPrettyFlow(config.getBool("RD_YAML_PRETTY", true));
        Representer representer = new Representer(dumperOptions);
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

    static class CmdFactory implements CommandLine.IFactory {
        private final RdTool rd;
        private final CommandLine.IFactory defaultFactory;

        public CmdFactory(RdTool rd) {
            this.rd = rd;
            defaultFactory = CommandLine.defaultFactory();
        }

        @Override
        public <K> K create(Class<K> cls) throws Exception {
            K k = defaultFactory.create(cls);
            if (k instanceof RdCommandExtension) {
                rd.initExtension(((RdCommandExtension) k));
            }
            return k;
        }
    }

    static List<RdCommandExtension> loadCommands(final Rd rd, RdToolImpl commandTool) {
        List<RdCommandExtension> extensions = ExtensionLoaderUtil.list();
        extensions.forEach(commandTool::initExtension);

        if (rd.getDebugLevel() > 0) {
            extensions.forEach(ext -> rd.getOutput().warning("# Including extension: " + ext.getClass().getName()));
        }
        return extensions;
    }

    public static void setup(final Rd rd, RdBuilder builder) {

        builder.printStackTrace(rd.getDebugLevel() > 0);
        setupFormat(builder, rd);

        boolean insecureSsl = rd.getBool(ENV_INSECURE_SSL, false);
        boolean insecureSslNoWarn = rd.getBool(ENV_INSECURE_SSL_NO_WARN, false);
        rd.setOutput(builder.finalOutput());
        if (insecureSsl && !insecureSslNoWarn) {
            rd.getOutput().warning(
                    "# WARNING: RD_INSECURE_SSL=true, no hostname or certificate trust verification will be performed");
        }
    }

    static class Rd extends ConfigBase implements RdApp, RdClientConfig, Closeable {
        private final Resources resources = new Resources();
        Client<RundeckApi> client;
        private CommandOutput output = new SystemOutput();

        public Rd(final ConfigValues src) {
            super(src);
        }

        public boolean isAnsiEnabled() {
            String term = getString("TERM", null);
            String rdColor = getString("RD_COLOR", null);

            String noColor = getString("NO_COLOR", null); // https://no-color.org/
            boolean noColorFlag = noColor != null && !noColor.isEmpty();

            boolean autoEnabled = term != null && term.contains("color");
            boolean enabled = "1".equals(rdColor);
            boolean disabled = "0".equals(rdColor) || noColorFlag;
            return enabled || (autoEnabled && !disabled);
        }

        @Override
        public int getDebugLevel() {
            return getInt(RD_DEBUG, 0);
        }

        public String getDateFormat() {
            return getString("RD_DATE_FORMAT", "yyyy-MM-dd'T'HH:mm:ssXX");
        }

        @Override
        public Client<RundeckApi> getClient() throws InputError {
            if (null == client) {
                try {
                    client = resources.add(Main.createClient(this));
                } catch (ConfigSourceError configSourceError) {
                    throw new InputError(configSourceError.getMessage());
                }
            }
            return client;
        }

        @Override
        public Client<RundeckApi> getClient(final int version) throws InputError {
            try {
                client = resources.add(Main.createClient(this, version));
            } catch (ConfigSourceError configSourceError) {
                throw new InputError(configSourceError.getMessage());
            }
            return client;
        }

        @Override
        public <T> ServiceClient<T> getClient(final Class<T> api, final int version) throws InputError {
            try {
                return resources.add(Main.createClient(this, api, version));
            } catch (ConfigSourceError configSourceError) {
                throw new InputError(configSourceError.getMessage());
            }
        }

        @Override
        public <T> ServiceClient<T> getClient(final Class<T> api) throws InputError {
            try {
                return resources.add(Main.createClient(this, api, null));
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

        @Override
        public void close() throws IOException {
            resources.close();
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
                RD_URL,
                "Please specify the Rundeck base URL, e.g. http://host:port or http://host:port/api/14"
        );

        if (!auth.isConfigured() && config.getBool(RD_AUTH_PROMPT, true) && null != System.console()) {
            auth = auth.chain(new ConsoleAuth(String.format("Credentials for URL: %s", baseUrl)).memoize());
        }
        RundeckClient.Builder<T> builder = RundeckClient.builder(api)
                                                        .baseUrl(baseUrl)
                                                        .config(config);
        if (null != requestedVersion) {
            builder.apiVersion(requestedVersion);
        } else {
            int anInt = config.getInt(RD_API_VERSION, -1);
            if (anInt > 0) {
                builder.apiVersion(anInt);
            }
        }

        if (auth.isTokenAuth()) {
            builder.tokenAuth(auth.getToken());
        } else if (auth.isSSOAuth()) {
            builder.bearerTokenAuth(auth.getBearerToken());
        } else {
            if (null == auth.getUsername() || "".equals(auth.getUsername().trim())) {
                throw new IllegalArgumentException("Username or token must be entered, or use environment variable " +
                                                   RD_USER + " or " + RD_TOKEN + " or "+RD_AUTH);
            }
            if (null == auth.getPassword() || "".equals(auth.getPassword().trim())) {
                throw new IllegalArgumentException("Password must be entered, or use environment variable " +
                                                   RD_PASSWORD);
            }
            builder.passwordAuth(auth.getUsername(), auth.getPassword());
        }
        builder.logger(new OutputLogger(config.getOutput()));
        builder.userAgent("rd-cli-tool/" + org.rundeck.client.Version.VERSION);
        return builder.build();

    }

    interface Auth {
        default boolean isConfigured() {
            return null != getToken() ||
                    null != getBearerToken() ||
                    (null != getUsername() && null != getPassword()
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
        default String getBearerToken() {
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
        default boolean isSSOAuth() {
            String username = getUsername();
            if (null != username && !username.trim().isEmpty()) {
                return false;
            }
            String token = getToken();
            if(null != token && !token.isEmpty()){
                return false;
            }
            return null != getBearerToken() && !"".equals(getBearerToken());
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
            return config.get(RD_USER);
        }

        @Override
        public String getPassword() {
            return config.get(RD_PASSWORD);
        }

        @Override
        public String getToken() {
            return config.get(RD_TOKEN);
        }
        @Override
        public String getBearerToken() {
            return config.get(RD_AUTH);
        }
    }

    static class ConsoleAuth implements Auth {
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
        @Override
        public String getBearerToken() {
            echo();
            char[] chars = System.console().readPassword("Enter Bearer token: ");
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

        @Override
        public String getBearerToken() {
            return findFirst(Auth::getBearerToken);
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

    @CommandLine.Command(name = "pond", hidden = true)
    public static class Something implements Runnable{
        public void run() {
            int i = new Random().nextInt(4);
            String kind;
            switch (i) {
                case 1:
                    kind = CommandLine.Help.Ansi.AUTO.string("@|blue A little luck.|@");
                    break;
                case 2:
                    kind = CommandLine.Help.Ansi.AUTO.string("@|green Good luck.|@");
                    break;
                case 3:
                    kind = CommandLine.Help.Ansi.AUTO.string("@|fg(215) Great luck.|@");
                    break;
                default:
                    kind = "Big trouble.";
                    break;
            }

            System.out.println("For your reference, today you will have:");
            System.out.println(kind);
        }
    }

    private static class OutputLogger implements Client.Logger {
        final CommandOutput output;

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
