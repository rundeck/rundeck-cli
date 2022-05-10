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
import picocli.CommandLine;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectItem;
import org.rundeck.client.tool.commands.projects.*;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;

import java.io.Console;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * projects subcommands
 */
@CommandLine.Command(
        description = "List and manage projects.",
        name = "projects",
        subcommands = {
                ACLs.class,
                SCM.class,
                Readme.class,
                Configure.class,
                Archives.class
        })
public class Projects extends BaseCommand {
    @CommandLine.Mixin
    ProjectListFormatOptions formatOptions;
    @CommandLine.Mixin
    VerboseOption verboseOption;

    @CommandLine.Command(description = "List all projects.")
    public void list() throws IOException, InputError {
        List<ProjectItem> body = apiCall(RundeckApi::listProjects);
        if (formatOptions.getOutputFormat() == null) {
            getRdOutput().info(String.format("%d Projects:%n", body.size()));
        }

        outputProjectList(body, ProjectItem::getName, ProjectItem::toMap);
    }

    @CommandLine.Command(
            description = "Get info about a project. Use -v/--verbose to output all available config data, or use " +
                    "-%/--outformat for selective data.")
    public void info(@CommandLine.Mixin ProjectNameOptions opts) throws IOException, InputError {
        String project = getRdTool().projectOrEnv(opts);
        ProjectItem body = apiCall(api -> api.getProjectInfo(project));

        outputProjectList(Collections.singletonList(body), ProjectItem::toBasicMap, ProjectItem::toMap);
    }

    private void outputProjectList(
            final List<ProjectItem> body,
            final Function<ProjectItem, Object> basicOutput,
            final Function<ProjectItem, Map<Object, Object>> verboseOutput
    ) {
        final Function<ProjectItem, ?> outformat;
        if (verboseOption.isVerbose()) {
            getRdOutput().output(body.stream().map(verboseOutput).collect(Collectors.toList()));
            return;
        }
        if (formatOptions.getOutputFormat() != null) {
            outformat = Format.formatter(formatOptions.getOutputFormat(), ProjectItem::toMap, "%", "");
        } else {
            outformat = basicOutput;
        }

        getRdOutput().output(body.stream().map(outformat).collect(Collectors.toList()));
    }

    @Getter @Setter
    static class ProjectDelete extends ProjectNameOptions {
        @CommandLine.Option(names = {"--confirm", "-y"}, description = "Force confirmation of delete request.")
        boolean confirm;

    }

    @CommandLine.Command(description = "Delete a project")
    public boolean delete(@CommandLine.Mixin ProjectDelete options) throws IOException, InputError {
        String project = getRdTool().projectOrEnv(options);
        if (!options.isConfirm()) {
            //request confirmation
            Console console = System.console();
            String s = "n";
            if (null != console) {
                s = console.readLine("Really delete project %s? (y/N) ", project);
            } else {
                getRdOutput().warning("No console input available, and --confirm/-y was not set.");
            }

            if (!"y".equals(s)) {
                getRdOutput().warning(String.format("Not deleting project %s.", project));
                return false;
            }
        }
        apiCall(api -> api.deleteProject(project));
        getRdOutput().info(String.format("Project was deleted: %s%n", project));
        return true;
    }

    @CommandLine.Command(description = "Create a project.")
    public void create(
            @CommandLine.Mixin ProjectNameOptions nameOptions,
            @CommandLine.Mixin Configure.ConfigFileOptions configFileOptions,
            @CommandLine.Mixin UnparsedConfigOptions options
    ) throws IOException, InputError {

        Map<String, String> config = Configure.loadConfig(configFileOptions, options, false);

        ProjectItem project = new ProjectItem();
        project.setName(getRdTool().projectOrEnv(nameOptions));
        project.setConfig(config);

        ProjectItem body = apiCall(api -> api.createProject(project));
        getRdOutput().info(String.format("Created project: \n\t%s%n", body.toBasicString()));
    }

}
