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
import okhttp3.RequestBody;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.util.Client;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;

import java.io.File;
import java.io.IOException;

@Command(description = "Upload a Rundeck plugin to your plugin repository",value="upload")
public class UploadPlugin implements RdCommandExtension {
    @Setter private RdTool rdTool;

    @CommandLineInterface
    interface UploadPluginOption {
        @Option(shortName = "r", longName = "repository", description = "Target name of repository to upload plugin into.")
        String getRepoName();
        @Option(shortName = "f", longName = "file", description = "Path to Rundeck 2.0 plugin to install in your repository")
        String getBinaryPath();
    }


    @Command(isDefault = true)
    public void upload(UploadPluginOption option,CommandOutput output) throws InputError, IOException {
        File binary = new File(option.getBinaryPath());
        if(!binary.exists()) throw new IOException(String.format("Unable to find specified file: %s",option.getBinaryPath()));
        RequestBody fileUpload = RequestBody.create(Client.MEDIA_TYPE_OCTET_STREAM, binary);

        RepositoryResponseHandler.handle(rdTool.apiWithErrorResponse(api -> api.uploadPlugin(option.getRepoName(), fileUpload)),output);
    }
}
