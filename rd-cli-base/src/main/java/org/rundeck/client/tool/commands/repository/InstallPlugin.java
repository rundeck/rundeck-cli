/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
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
package org.rundeck.client.tool.commands.repository;

import lombok.Setter;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdOutput;
import org.rundeck.client.tool.extension.RdTool;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Install a plugin from your plugin repository into your Rundeck instance", name = "install")
public class InstallPlugin implements RdCommandExtension, RdOutput, Callable<Integer> {
    @Setter
    private RdTool rdTool;

    @Setter
    private CommandOutput rdOutput;
    @CommandLine.Option(names = {"-r", "--repository"}, description = "Repository name that contains the plugin.", required = true)
    String repoName;
    @CommandLine.Option(names = {"--id", "-i"}, description = "Id of the plugin you want to install", required = true)
    String pluginId;
    @CommandLine.Option(names = {"--version", "-v"}, description = "(Optional) Specific version of the plugin you want to install")
    String version;

    public Integer call() throws InputError, IOException {
        RepositoryResponseHandler.handle(
                rdTool.apiWithErrorResponse(api -> {
                    if (version != null) {
                        return api.installPlugin(repoName, pluginId, version);
                    } else {
                        return api.installPlugin(repoName, pluginId);
                    }
                }), rdOutput);
        return 0;
    }

}
