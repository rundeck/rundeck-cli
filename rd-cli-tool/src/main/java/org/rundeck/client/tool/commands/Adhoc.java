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

package org.rundeck.client.tool.commands;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.rundeck.client.api.model.AdhocResponse;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Quoting;
import org.rundeck.client.util.Util;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;


/**
 * adhoc subcommands
 */

@CommandLine.Command(description = "Run adhoc command or script on matching nodes.", name = "adhoc")
public class Adhoc extends BaseCommand implements Callable<Boolean> {

    @CommandLine.Mixin
    AdhocBaseOptions options;
    @CommandLine.Mixin
    ExecutionOutputFormatOption outputFormatOption;
    @CommandLine.Mixin
    FollowOptions followOptions;
    @CommandLine.Mixin
    NodeFilterOptions nodeFilterOptions;

    @CommandLine.Command()
    public Boolean call() throws IOException, InputError {
        AdhocResponse adhocResponse;

        String project = getRdTool().projectOrEnv(options);
        if (options.getScriptFile() != null || options.isStdin()) {
            RequestBody scriptFileBody;
            String filename;
            if (options.getScriptFile() != null) {
                File input = options.getScriptFile();
                if (!input.canRead() || !input.isFile()) {
                    throw new InputError(String.format(
                            "File is not readable or does not exist: %s",
                            input
                    ));
                }

                scriptFileBody = RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        input
                );
                filename = input.getName();
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                long bytes = Util.copyStream(System.in, byteArrayOutputStream);

                scriptFileBody = RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        byteArrayOutputStream.toByteArray()

                );
                filename = "script.sh";
            }

            adhocResponse = getRdTool().apiCall(api -> api.runScript(
                    project,
                    MultipartBody.Part.createFormData("scriptFile", filename, scriptFileBody),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    Quoting.joinStringQuoted(options.getCommandString()),
                    options.getScriptInterpreter(),
                    options.isArgsQuoted(),
                    options.getFileExtension(),
                    nodeFilterOptions.getFilter()
            ));
        } else if (options.getUrl() != null) {
            adhocResponse = getRdTool().apiCall(api -> api.runUrl(
                    project,
                    options.getUrl(),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    Quoting.joinStringQuoted(options.getCommandString()),
                    options.getScriptInterpreter(),
                    options.isArgsQuoted(),
                    options.getFileExtension(),
                    nodeFilterOptions.getFilter()
            ));
        } else if (options.getCommandString() != null && options.getCommandString().size() > 0) {
            //command
            adhocResponse = getRdTool().apiCall(api -> api.runCommand(
                    project,
                    Quoting.joinStringQuoted(options.getCommandString()),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    nodeFilterOptions.getFilter()
            ));
        } else {
            throw new InputError("-s, -u, or -- command string, was expected");
        }


        Execution execution = getRdTool().apiCall(api -> api.getExecution(adhocResponse.execution.getId()));
        if (followOptions.isFollow()) {
            getRdOutput().info("Started execution " + execution.toExtendedString(getRdTool().getAppConfig()));
        } else {
            if (outputFormatOption.getOutputFormat() == null) {
                getRdOutput().info(adhocResponse.message);
            }
            Executions.outputExecutionList(
                    outputFormatOption,
                    getRdOutput(),
                    getRdTool().getAppConfig(),
                    Collections.singletonList(execution).stream()
            );
        }

        return Executions.maybeFollow(getRdTool(), followOptions, outputFormatOption, adhocResponse.execution.getId(), getRdOutput());
    }

}
