package org.rundeck.client.tool.commands.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.OptionOrder;
import com.lexicalscope.jewel.cli.Unparsed;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.model.ProjectConfig;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.options.OptionUtil;
import org.rundeck.client.tool.options.ProjectCreateOptions;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.UnparsedConfigOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Subcommands for project configuration
 *
 * @author greg
 * @since 2/2/17
 */
@Command(description = "Manage Project configuration")
public class Configure extends AppCommand {
    public Configure(final RdApp rdApp) {
        super(rdApp);
    }

    @CommandLineInterface(application = "get", order = OptionOrder.DEFINITION) interface ConfigureGetOpts extends
            ProjectNameOptions
    {


    }

    @Command(description = "Get all configuration properties for a project. (Supports RD_FORMAT=properties env var)")
    public void get(ConfigureGetOpts opts, CommandOutput output) throws IOException, InputError {
        ProjectConfig config = apiCall(api -> api.getProjectConfiguration(opts.getProject()));

        if ("properties".equals(getAppConfig().getString("RD_FORMAT", null))) {
            Properties properties = new Properties();
            properties.putAll(config.getConfig());
            properties.store(System.out, "rd");
        } else {
            output.output(config.getConfig());
        }
    }

    enum InputFileFormat {
        properties,
        json,
        yaml
    }

    interface ConfigFileOptions {
        @Option(shortName = "f",
                longName = "file",
                description = "Input file for project configuration. Can be a .properties, .json or .yaml file. " +
                              "Format is determined by file extension or -F/--format")
        File getFile();

        boolean isFile();

        @Option(shortName = "F",
                longName = "format",
                description = "Input file format. Can be [properties, json, yaml] (default: properties, unless " +
                              "recognized in filename)")
        InputFileFormat getFileFormat();

        boolean isFileFormat();
    }

    interface ConfigInputOptions extends ConfigFileOptions, UnparsedConfigOptions {

    }

    @CommandLineInterface(application = "set", order = OptionOrder.DEFINITION) interface ConfigureSetOpts extends
            ConfigInputOptions,
            ProjectNameOptions
    {


    }

    @Command(description = "Overwrite all configuration properties for a project. Any config keys not included will " +
                           "be " +
                           "removed.")
    public boolean set(ConfigureSetOpts opts, CommandOutput output) throws IOException, InputError {

        Map<String, String> config = loadConfig(opts);
        ProjectConfig projectConfig = apiCall(api -> api.setProjectConfiguration(
                opts.getProject(),
                new ProjectConfig(config)
        ));

        return true;
    }

    private Map<String, String> loadConfig(final ConfigInputOptions opts) throws InputError, IOException {
        HashMap<String, String> inputConfig = new HashMap<>();
        if (opts.isFile()) {
            File input = opts.getFile();
            InputFileFormat format = opts.getFileFormat();
            if (null == format) {
                format = InputFileFormat.properties;
                if (input.getName().endsWith(".properties")) {
                    format = InputFileFormat.properties;
                } else if (input.getName().endsWith(".json")) {
                    format = InputFileFormat.json;
                } else if (input.getName().endsWith(".yaml") || input.getName().endsWith(".yml")) {
                    format = InputFileFormat.yaml;
                }
            }
            switch (format) {
                case properties:
                    try (FileInputStream fis = new FileInputStream(input)) {
                        Properties properties = new Properties();
                        properties.load(fis);
                        for (Object key : properties.keySet()) {
                            inputConfig.put(key.toString(), properties.getProperty(key.toString()));
                        }
                    }
                    break;
                case json:
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map map = objectMapper.readValue(input, Map.class);
                    inputConfig.putAll(map);
                    break;
                case yaml:
                    Yaml yaml = new Yaml(new SafeConstructor());
                    try (FileInputStream fis = new FileInputStream(input)) {
                        Object load = yaml.load(fis);
                        if (load instanceof Map) {
                            inputConfig.putAll((Map) load);
                        } else {
                            throw new InputError("Yaml.load: data is not a Map");
                        }
                    }
                    break;
            }
        }
        if (opts.config() != null && opts.config().size() > 0) {
            Map<String, String> config = OptionUtil.parseKeyValueMap(opts.config());
            inputConfig.putAll(config);
        }

        if (inputConfig.size() < 1) {
            throw new InputError("no configuration was specified");
        }
        return inputConfig;
    }

    @CommandLineInterface(application = "update", order = OptionOrder.DEFINITION) interface ConfigureUpdateOpts extends
            ConfigInputOptions,
            ProjectNameOptions
    {


    }

    @Command(description = "Modify configuration properties for a project. Only the specified keys will be updated. " +
                           "Can provide input via a file (json, properties or yaml), or commandline. If both are " +
                           "provided, the commandline values will override the loaded file values.")
    public boolean update(ConfigureUpdateOpts opts, CommandOutput output) throws IOException, InputError {
        Map<String, String> config = loadConfig(opts);
        output.info(String.format("Updating %d configuration properties...", config.size()));
        for (String s : config.keySet()) {
            ProjectConfig body = new ProjectConfig(Collections.singletonMap("value", config.get(s)));
            ProjectConfig result = apiCall(api -> api.setProjectConfigurationKey(opts.getProject(), s, body));
            output.info("Updated value: " + result.getConfig());
        }
        return true;
    }

    @CommandLineInterface(application = "delete", order = OptionOrder.DEFINITION) interface ConfigureDeleteOpts extends
            ProjectNameOptions
    {

        @Unparsed(name = "-- key [key [key..]]",
                  defaultValue = {},
                  description = "A list of config keys to remove, space separated after a '--' separator. ")
        List<String> config();

    }

    @Command(description = "Remove configuration properties for a project. All")
    public boolean delete(ConfigureDeleteOpts opts, CommandOutput output) throws IOException, InputError {
        List<String> removeKeys = opts.config();

        if (removeKeys.size() < 1) {
            throw new InputError("use `-- key1 key2` to specify keys to delete");
        }
        output.info(String.format("Removing %d configuration properties...", removeKeys.size()));
        for (String s : removeKeys) {
            Void result = apiCall(api -> api.deleteProjectConfigurationKey(opts.getProject(), s));
            output.info("Removed key: " + s);

        }
        return true;
    }
}
