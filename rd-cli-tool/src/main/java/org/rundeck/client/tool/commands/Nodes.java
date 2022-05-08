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


import lombok.Data;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.api.model.ProjectNode;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.NodeFilterOptions;
import org.rundeck.client.tool.options.NodeOutputFormatOption;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.client.util.Format;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author greg
 * @since 11/22/16
 */
@CommandLine.Command(description = "List node resources.", name = "nodes")
public class Nodes extends BaseCommand {


    @CommandLine.Mixin
    ProjectNameOptions options;
    @CommandLine.Mixin
    NodeOutputFormatOption nodeOutputFormatOption;
    @CommandLine.Mixin
    NodeFilterOptions nodeFilterOptions;

    @CommandLine.Command(description = "List all nodes for a project.  You can use the -F/--filter to specify a node filter, or " +
            "simply add the filter on the end of the command")
    public void list() throws IOException, InputError {
        String project = getRdTool().projectOrEnv(options);
        Map<String, ProjectNode> body = apiCall(api -> api.listNodes(project, nodeFilterOptions.filterString()));
        if (!nodeOutputFormatOption.isOutputFormat()) {
            getRdOutput().info(String.format("%d Nodes%s in project %s:%n", body.size(),
                    nodeFilterOptions.isFilter() ? " matching filter" : "",
                    project
            ));
        }
        Function<ProjectNode, ?> field;
        if (nodeOutputFormatOption.isOutputFormat()) {
            field = Format.formatter(nodeOutputFormatOption.getOutputFormat(), ProjectNode::getAttributes, "%", "");
        } else if (nodeOutputFormatOption.isVerbose()) {
            field = ProjectNode::getAttributes;
        } else {
            field = ProjectNode::getName;
        }
        getRdOutput().output(body.values().stream().map(field).collect(Collectors.toList()));
    }
}
