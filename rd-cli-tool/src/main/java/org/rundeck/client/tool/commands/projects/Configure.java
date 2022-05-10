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

package org.rundeck.client.tool.commands.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.api.model.ProjectConfig;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.OptionUtil;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.UnparsedConfigOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Subcommands for project configuration
 *
 * @author greg
 * @since 2/2/17
 */
@CommandLine.Command(description = "Manage Project configuration", name = "configure")
public class Configure extends BaseCommand {


    @CommandLine.Command(description = "Get all configuration properties for a project. (Supports RD_FORMAT=properties env var)")
    public void get(@CommandLine.Mixin ProjectNameOptions opts) throws IOException, InputError {
        ProjectConfig config = apiCall(api -> api.getProjectConfiguration(opts.getProject()));

        if ("properties".equals(getRdTool().getAppConfig().getString("RD_FORMAT", null))) {
            Properties properties = new Properties();
            properties.putAll(config.getConfig());
            properties.store(System.out, "rd");
        } else {
            getRdOutput().output(config.getConfig());
        }
    }

    public static enum InputFileFormat {
        properties,
        json,
        yaml
    }

    @Getter @Setter
    public static class ConfigFileOptions {
        @CommandLine.Option(names = {"-f", "--file"},
                description = "Input file for project configuration. Can be a .properties, .json or .yaml file. " +
                        "Format is determined by file extension or -F/--format")
        File file;


        @CommandLine.Option(names = {"-F", "--format"},
                description = "Input file format. Can be [properties, json, yaml] (default: properties, unless " +
                        "recognized in filename)")
        Configure.InputFileFormat fileFormat;

    }


    @CommandLine.Command(description = "Overwrite all configuration properties for a project. Any config keys not included will " +
            "be " +
            "removed.", showEndOfOptionsDelimiterInUsageHelp = true)
    public void set(@CommandLine.Mixin ConfigFileOptions configFileOptions,
                    @CommandLine.Mixin UnparsedConfigOptions unparsedConfigOptions,
                    @CommandLine.Mixin ProjectNameOptions opts) throws IOException, InputError {

        Map<String, String> config = loadConfig(configFileOptions, unparsedConfigOptions, true);
        apiCall(api -> api.setProjectConfiguration(
                opts.getProject(),
                new ProjectConfig(config)
        ));
    }

    public static Map<String, String> loadConfig(final ConfigFileOptions opts,
                                                 UnparsedConfigOptions unparsedConfigOptions,
                                                 final boolean requireInput) throws InputError, IOException {
        HashMap<String, String> inputConfig = new HashMap<>();
        if (opts.getFile() != null) {
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
                    for (Object o : map.keySet()) {
                        if (!(o instanceof String)) {
                            throw new InputError(String.format(
                                    "Expected all keys of the json object to be strings, but saw: %s for: %s",
                                    o.getClass(),
                                    o
                            ));
                        }
                        Object value = map.get(o);
                        if (!(value instanceof String)) {
                            throw new InputError(String.format(
                                    "Expected all values of the json object to be strings, but saw: %s for: %s, for "
                                    + "key: %s",
                                    value.getClass(),
                                    value,
                                    o
                            ));
                        }
                    }
                    //noinspection unchecked
                    inputConfig.putAll(map);
                    break;
                case yaml:
                    Yaml yaml = new Yaml(new SafeConstructor());
                    try (FileInputStream fis = new FileInputStream(input)) {
                        Object load = yaml.load(fis);
                        if (load instanceof Map) {
                            //noinspection unchecked
                            inputConfig.putAll((Map) load);
                        } else {
                            throw new InputError("Yaml.load: data is not a Map");
                        }
                    }
                    break;
            }
        }
        if (unparsedConfigOptions.getConfig() != null && unparsedConfigOptions.getConfig().size() > 0) {
            Map<String, String> config = OptionUtil.parseKeyValueMap(unparsedConfigOptions.getConfig());
            inputConfig.putAll(config);
        }

        if (inputConfig.size() < 1 && requireInput) {
            throw new InputError("no configuration was specified");
        }
        return inputConfig;
    }


    @CommandLine.Command(description = "Modify configuration properties for a project. Only the specified keys will be updated. " +
            "Can provide input via a file (json, properties or yaml), or commandline. If both are " +
            "provided, the commandline values will override the loaded file values.",
            showEndOfOptionsDelimiterInUsageHelp = true)
    public void update(@CommandLine.Mixin ConfigFileOptions configFileOptions,
                       @CommandLine.Mixin UnparsedConfigOptions unparsedConfigOptions,
                       @CommandLine.Mixin ProjectNameOptions opts) throws IOException, InputError {
        Map<String, String> config = loadConfig(configFileOptions, unparsedConfigOptions, true);
        getRdOutput().info(String.format("Updating %d configuration properties...", config.size()));
        for (String s : config.keySet()) {
            ProjectConfig body = new ProjectConfig(Collections.singletonMap("value", config.get(s)));
            ProjectConfig result = apiCall(api -> api.setProjectConfigurationKey(opts.getProject(), s, body));
            getRdOutput().info("Updated value: " + result.getConfig());
        }
    }

    @Getter @Setter
    static class ConfigureDeleteOpts extends
            ProjectNameOptions {

        @CommandLine.Parameters(paramLabel = "key [key [key..]]",
                description = "A list of config keys to remove, space separated after a '--' separator. ")
        List<String> config;
    }

    @CommandLine.Command(description = "Remove configuration properties for a project.",
            showEndOfOptionsDelimiterInUsageHelp = true)
    public void delete(@CommandLine.Mixin ConfigureDeleteOpts opts) throws IOException, InputError {
        List<String> removeKeys = opts.getConfig();

        if (removeKeys.size() < 1) {
            throw new InputError("use `-- key1 key2` to specify keys to delete");
        }
        getRdOutput().info(String.format("Removing %d configuration properties...", removeKeys.size()));
        for (String s : removeKeys) {
            Void result = apiCall(api -> api.deleteProjectConfigurationKey(opts.getProject(), s));
            getRdOutput().info("Removed key: " + s);

        }
    }
}
