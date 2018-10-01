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
import okhttp3.RequestBody;
import org.rundeck.client.api.model.verb.ArtifactActionMessage;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.util.Client;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;

import java.io.File;
import java.io.IOException;

@Command(description = "Upload a Rundeck plugin to your plugin repository",value="upload")
public class UploadPlugin extends AppCommand {
    public UploadPlugin(final RdApp rdApp) {
        super(rdApp);
    }

    @CommandLineInterface
    interface UploadPluginOption {
        @Option(shortName = "f", longName = "file", description = "Path to Rundeck 2.0 plugin to install in your repository")
        String getBinaryPath();
    }


    @Command(isDefault = true)
    public void upload(UploadPluginOption option,CommandOutput output) throws InputError, IOException {
        File binary = new File(option.getBinaryPath());
        if(!binary.exists()) throw new IOException(String.format("Unable to find specified file: %s",option.getBinaryPath()));
        RequestBody fileUpload = RequestBody.create(Client.MEDIA_TYPE_OCTET_STREAM, binary);
        ArtifactActionMessage msg = apiCall(api -> api.uploadPlugin(fileUpload));
        if(msg.getErrors() != null && !msg.getErrors().isEmpty()) {
            msg.getErrors().forEach(error -> {
                output.error(error.getMsg());
            });
        } else {
            output.output(msg.getMsg());
        }
    }
}
