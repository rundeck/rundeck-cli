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

package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.rundeck.client.api.ReadmeFile;
import org.rundeck.client.api.model.ProjectReadme;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.ProjectNameOptions;

import java.io.File;
import java.io.IOException;


/**
 * projects readme subcommands
 */
@Command(description = "Manage Project readme.md/motd.md")
public class Readme extends AppCommand {
    public Readme(final RdApp client) {
        super(client);
    }

    public interface GetOptions extends ProjectNameOptions {
        @Option(shortName = "m",
                longName = "motd",
                description = "Choose the 'motd.md' file. If unset, choose 'readme.md'.")
        boolean isMotd();

    }

    public ReadmeFile getReadmeFile(GetOptions options) {
        return options.isMotd() ? ReadmeFile.MOTD : ReadmeFile.README;
    }

    @Command(description = "get project readme/motd file")
    public void get(GetOptions options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ProjectReadme readme = apiCall(api -> api.getReadme(project, getReadmeFile(options)));
        output.output(readme.getContents());
    }


    public interface SetOptions extends GetOptions {
        @Option(shortName = "f", longName = "file", description = "Path to a file to read for readme/motd contents.")
        File getFile();

        boolean isFile();

        @Option(shortName = "t", longName = "text", description = "Text to use for readme/motd contents.")
        String getText();

        boolean isText();
    }


    @Command(description = "set project readme/motd file")
    public void put(SetOptions options, CommandOutput output) throws IOException, InputError {
        if (!options.isText() && !options.isFile()) {
            throw new InputError("-f/--file or -t/--text is required");
        }
        RequestBody requestBody;
        if (options.isFile()) {
            requestBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    options.getFile()
            );
        } else {
            requestBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    options.getText()
            );
        }
        String project = projectOrEnv(options);
        ProjectReadme readme = apiCall(api -> api.putReadme(project, getReadmeFile(options), requestBody));
        output.output(readme.getContents());
    }


    @Command(description = "delete project readme/motd file")
    public void delete(GetOptions options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Void readme = apiCall(api -> api.deleteReadme(project, getReadmeFile(options)));
        output.info(String.format("Deleted %s for project %s", getReadmeFile(options), project));
    }
}
