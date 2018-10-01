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
package org.rundeck.client.tool.commands.verb;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.verb.ArtifactActionMessage;
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
        @Option(longName = "id", description = "Id of the plugin you want to install")
        String getPluginId();
    }

    @Command(isDefault = true)
    public void install(InstallPluginOption option, CommandOutput output) throws InputError, IOException {
        String pluginId = option.getPluginId();
        ArtifactActionMessage msg = apiCall(api -> api.installPlugin(pluginId));
        if(msg.getErrors() != null && !msg.getErrors().isEmpty()) {
            msg.getErrors().forEach(error -> {
                output.error(error.getMsg());
            });
        } else {
            output.output(msg.getMsg());
        }
    }

}
