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

import okhttp3.RequestBody;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.util.Client;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Upload a Rundeck plugin to your plugin repository", name = "upload")
public class UploadPlugin extends BaseCommand implements Callable<Boolean> {

    @CommandLine.Option(names = {"-r", "--repository"}, description = "Target name of repository to upload plugin into.", required = true)
    String repoName;

    @CommandLine.Option(names = {"-f", "--file"}, description = "Path to Rundeck 2.0 plugin to install in your repository", required = true)
    String binaryPath;


    public Boolean call() throws InputError, IOException {
        File binary = new File(binaryPath);
        if (!binary.exists())
            throw new IOException(String.format("Unable to find specified file: %s", binaryPath));
        RequestBody fileUpload = RequestBody.create(binary, Client.MEDIA_TYPE_OCTET_STREAM);

        RepositoryResponseHandler.handle(getRdTool().apiWithErrorResponse(api -> api.uploadPlugin(repoName, fileUpload)), getRdOutput());
        return true;
    }
}
