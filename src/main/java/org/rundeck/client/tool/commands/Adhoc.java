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

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.rundeck.client.api.model.AdhocResponse;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.AdhocBaseOptions;
import org.rundeck.client.util.Quoting;
import org.rundeck.client.util.Util;
import retrofit2.Call;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;



/**
 * adhoc subcommands
 */

@Command(description = "Run adhoc command or script on matching nodes.")
public class Adhoc extends AppCommand {
    static final String COMMAND = "adhoc";

    public Adhoc(final RdApp client) {
        super(client);
    }

    @CommandLineInterface(application = COMMAND) interface AdhocOptions extends AdhocBaseOptions,
            Executions.ExecutionResultOptions
    {

    }

    @Command(isSolo = true, isDefault = true)
    public boolean adhoc(AdhocOptions options, CommandOutput output) throws IOException, InputError {
        Call<AdhocResponse> adhocResponseCall = null;

        String project = projectOrEnv(options);
        if (options.isScriptFile() || options.isStdin()) {
            RequestBody scriptFileBody;
            String filename;
            if (options.isScriptFile()) {
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

            adhocResponseCall = getClient().getService().runScript(
                    project,
                    MultipartBody.Part.createFormData("scriptFile", filename, scriptFileBody),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    Quoting.joinStringQuoted(options.getCommandString()),
                    null,
                    false,
                    null,
                    options.getFilter()
            );
        } else if (options.isUrl()) {
            adhocResponseCall = getClient().getService().runUrl(
                    project,
                    options.getUrl(),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    Quoting.joinStringQuoted(options.getCommandString()),
                    null,
                    false,
                    null,
                    options.getFilter()
            );
        } else if (options.getCommandString() != null && options.getCommandString().size() > 0) {
            //command
            adhocResponseCall = getClient().getService().runCommand(
                    project,
                    Quoting.joinStringQuoted(options.getCommandString()),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    options.getFilter()
            );
        } else {
            throw new InputError("-s, -u, or -- command string, was expected");
        }

        AdhocResponse adhocResponse = getClient().checkError(adhocResponseCall);

        Execution execution = apiCall(api -> api.getExecution(adhocResponse.execution.getId()));
        if (options.isFollow()) {
            output.info("Started execution " + execution.toExtendedString(getAppConfig()));
        } else {
            if (!options.isOutputFormat()) {
                output.info(adhocResponse.message);
            }
            Executions.outputExecutionList(options, output, Collections.singletonList(execution), getAppConfig());
        }

        return Executions.maybeFollow(getClient(), options, adhocResponse.execution.getId(), output);
    }

}
