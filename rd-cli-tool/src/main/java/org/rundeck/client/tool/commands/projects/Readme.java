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

import lombok.Setter;
import lombok.Getter;
import org.rundeck.client.tool.ProjectInput;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.ProjectRequiredNameOptions;
import picocli.CommandLine;
import org.rundeck.client.tool.InputError;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.rundeck.client.api.ReadmeFile;
import org.rundeck.client.api.model.ProjectReadme;

import java.io.File;
import java.io.IOException;


/**
 * projects readme subcommands
 */
@CommandLine.Command(description = "Manage Project readme.md/motd.md", name = "readme")
public class Readme extends BaseCommand {

    @Getter
    @Setter
    static class BaseOptions extends ProjectNameOptions {
        @CommandLine.Option(names = {"-m", "--motd"},
                description = "Choose the 'motd.md' file. If unset, choose 'readme.md'.")
        private boolean motd;

        public ReadmeFile getReadmeFile() {
            return isMotd() ? ReadmeFile.MOTD : ReadmeFile.README;
        }
    }

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec; // injected by picocli

    String validate(ProjectInput input) throws InputError {
        if (null != input.getProject()) {
            ProjectRequiredNameOptions.validateProjectName(input.getProject(), spec);
        }
        return getRdTool().projectOrEnv(input);
    }


    @CommandLine.Command(description = "get project readme/motd file")
    public void get(@CommandLine.Mixin BaseOptions opts) throws IOException, InputError {
        String project = validate(opts);
        ProjectReadme readme = apiCall(api -> api.getReadme(project, opts.getReadmeFile()));
        getRdOutput().output(readme.getContents());
    }


    @Getter @Setter
    public static class SetOptions {
        @CommandLine.Option(names = {"-f", "--file"}, description = "Path to a file to read for readme/motd contents.")
        File file;

        boolean isFile() {
            return file != null;
        }

        @CommandLine.Option(names = {"-t", "--text"}, description = "Text to use for readme/motd contents.")
        String text;

        boolean isText() {
            return text != null;
        }
    }


    @CommandLine.Command(description = "set project readme/motd file")
    public void put(@CommandLine.Mixin SetOptions options, @CommandLine.Mixin BaseOptions opts) throws IOException, InputError {
        if (!options.isText() && !options.isFile()) {
            throw new InputError("-f/--file or -t/--text is required");
        }
        RequestBody requestBody;
        if (options.isFile()) {
            requestBody = RequestBody.create(
                    options.getFile(),
                    MediaType.parse("text/plain")
            );
        } else {
            requestBody = RequestBody.create(
                    options.getText(),
                    MediaType.parse("text/plain")
            );
        }
        String project = validate(opts);
        ProjectReadme readme = apiCall(api -> api.putReadme(project, opts.getReadmeFile(), requestBody));
        getRdOutput().output(readme.getContents());
    }


    @CommandLine.Command(description = "delete project readme/motd file")
    public void delete(@CommandLine.Mixin BaseOptions opts) throws IOException, InputError {
        String project = validate(opts);
        Void readme = apiCall(api -> api.deleteReadme(project, opts.getReadmeFile()));
        getRdOutput().info(String.format("Deleted %s for project %s", opts.getReadmeFile(), project));
    }
}
