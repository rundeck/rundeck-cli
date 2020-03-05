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
import lombok.Setter;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;

import java.io.IOException;

@Command(description = "Unistall a Rundeck plugin from your Rundeck instance",value = "uninstall")
public class UninstallPlugin implements RdCommandExtension {
   @Setter private RdTool rdTool;

    @CommandLineInterface
    interface UninstallPluginOption {
        @Option(longName = "id", shortName = "i", description = "Id of the plugin you want to uninstall")
        String getPluginId();
    }

    @Command(isDefault = true)
    public void uninstall(UninstallPluginOption option, CommandOutput output) throws InputError, IOException {
        String pluginId = option.getPluginId();
        RepositoryResponseHandler.handle(
                rdTool.apiWithErrorResponse(api -> api.uninstallPlugin(pluginId)),output);
    }
}
