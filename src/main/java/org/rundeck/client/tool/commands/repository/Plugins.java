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
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.HasSubCommands;
import org.rundeck.toolbelt.InputError;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Command(description = "Manage Rundeck plugins")
public class Plugins extends AppCommand implements HasSubCommands {

    public Plugins(final RdApp rdApp) {
        super(rdApp);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(new UploadPlugin(getRdApp()),
                             new InstallPlugin(getRdApp()),
                             new UninstallPlugin(getRdApp()));
    }

    @Command(isDefault = true, description = "List plugins")
    public void list(CommandOutput output) throws InputError, IOException {
        List<RepositoryArtifacts> repos = apiCall(RundeckApi::listPlugins);
        repos.forEach(repo -> {
            //Ignore repo name for now
            //output.output("Repository: " + repo.getRepositoryName());
            repo.getResults().forEach(plugin -> {
                output.output(String.format("%s : %s (%sinstalled)",plugin.getId(), plugin.getName(), plugin.isInstalled() ? "":"not "));
            });
        });
    }
}
