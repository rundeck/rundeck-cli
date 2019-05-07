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

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;

import java.io.IOException;

@Command(description = "Install a plugin from your plugin repository into your Rundeck instance",value="install")
public class InstallPlugin extends AppCommand {
    public InstallPlugin(final RdApp rdApp) {
        super(rdApp);
    }

    @CommandLineInterface interface InstallPluginOption {
        @Option(shortName = "r", longName = "repository", description = "Repository name that contains the plugin.")
        String getRepoName();
        @Option(longName = "id",shortName = "i",description = "Id of the plugin you want to install")
        String getPluginId();
        @Option(longName = "version", shortName = "v",description = "(Optional) Specific version of the plugin you want to install",defaultToNull = true)
        String getVersion();
    }

    @Command(isDefault = true)
    public void install(InstallPluginOption option, CommandOutput output) throws InputError, IOException {
        String pluginId = option.getPluginId();

        RepositoryResponseHandler.handle(
                apiWithErrorResponse(api -> {
            if(option.getVersion() != null) {
                return api.installPlugin(option.getRepoName(),pluginId,option.getVersion());
            } else {
                return api.installPlugin(option.getRepoName(), pluginId);
            }
        }),output);

    }

}
