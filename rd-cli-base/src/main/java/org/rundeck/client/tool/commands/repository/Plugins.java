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

import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.repository.RepositoryArtifacts;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import picocli.CommandLine;


import java.io.IOException;
import java.util.List;

@CommandLine.Command(name = "plugins", description = "Manage Rundeck plugins", subcommands = {
        UploadPlugin.class,
        InstallPlugin.class,
        UninstallPlugin.class
})
public class Plugins
        extends BaseCommand {


    @CommandLine.Command(name = "list", description = "List plugins")
    public void list() throws InputError, IOException {
        List<RepositoryArtifacts> repos = getRdTool().apiCall(RundeckApi::listPlugins);
        repos.forEach(repo -> {
            getRdOutput().output("==" + repo.getRepositoryName() + " Repository==");
            repo.getResults().forEach(plugin -> {
                if (plugin.getInstallId() != null && !plugin.getInstallId().isEmpty()) {
                    String updateable = "";
                    if (plugin.isUpdatable()) {
                        updateable = " (Updatable to " + plugin.getCurrentVersion() + ")";
                    }
                    getRdOutput().output(String.format(
                            "%s : %s : %s (%sinstalled) %s",
                            plugin.getInstallId(),
                            plugin.getName(),
                            plugin.isInstalled() ? plugin.getInstalledVersion() : plugin.getCurrentVersion(),
                            plugin.isInstalled() ? "" : "not ",
                            updateable
                    ));
                }
            });
        });
    }
}
