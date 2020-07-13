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

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.toolbelt.ANSIColorOutput;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.client.tool.InputError;
import okhttp3.RequestBody;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ACLPolicy;
import org.rundeck.client.api.model.ACLPolicyItem;
import org.rundeck.client.api.model.ACLPolicyValidation;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.ACLOutputOptions;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.tool.util.Colorz;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.ServiceClient;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * projects acls subcommands
 */
@Command(description = "Manage Project ACLs")
public class ACLs extends AppCommand {
    public ACLs(final RdApp client) {
        super(client);
    }


    interface ListCommandOptions extends ProjectNameOptions, ACLOutputOptions {

    }
    @Command(description = "list project acls")
    public void list(ListCommandOptions options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ACLPolicyItem items = apiCall(api -> api.listAcls(project));
        outputListResult(options, output, items, String.format("project %s", project));
    }

    public static void outputListResult(
            final ACLOutputOptions options,
            final CommandOutput output,
            final ACLPolicyItem aclList,
            final String ident
    )
    {
        output.info(String.format(
                "%d ACL Policy items for %s",
                aclList.getResources().size(),
                ident
        ));
        final Function<ACLPolicyItem, ?> outformat;
        if (options.isVerbose()) {
            output.output(aclList.getResources().stream().map(ACLPolicyItem::toMap).collect(Collectors.toList()));
            return;
        } else if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), ACLPolicyItem::toMap, "%", "");
        } else {
            outformat = ACLPolicyItem::getPath;

        }
        output.output(aclList.getResources()
                             .stream()
                             .map(outformat)
                             .collect(Collectors.toList()));
    }

    @CommandLineInterface(application = "get") interface Get extends ACLNameOptions, ProjectNameOptions {
    }

    @Command(description = "get a project ACL definition")
    public void get(Get options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ACLPolicy aclPolicy = apiCall(api -> api.getAclPolicy(project, options.getName()));
        outputPolicyResult(output, aclPolicy);
    }

    public static void outputPolicyResult(final CommandOutput output, final ACLPolicy aclPolicy) {
        output.output(aclPolicy.getContents());
    }


    @CommandLineInterface(application = "upload") interface Put extends ProjectNameOptions, ACLFileOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();

    }

    @Command(description = "Upload a project ACL definition")
    public void upload(Put options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body, RundeckApi api) -> api.updateAclPolicy(project, options.getName(), body),
                this,
                output
        );
        outputPolicyResult(output, aclPolicy);
    }

    @CommandLineInterface(application = "create") interface Create extends ProjectNameOptions, ACLFileOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();

    }

    @Command(description = "Create a project ACL definition")
    public void create(Create options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body, RundeckApi api) -> api.createAclPolicy(project, options.getName(), body),
                this,
                output
        );
        outputPolicyResult(output, aclPolicy);
    }

    /**
     * Upload a file to create/modify an ACLPolicy
     *
     * @param options file options
     * @param func    create the request
     * @param rdTool  rdTool
     * @param output  output
     *
     * @return result policy
     *
     */
    public static ACLPolicy performACLModify(
            final ACLFileOptions options,
            BiFunction<RequestBody, RundeckApi, Call<ACLPolicy>> func,
            final RdTool rdTool,
            final CommandOutput output
    )
            throws IOException, InputError
    {

        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new InputError(String.format("File is not readable or does not exist: %s", input));
        }

        RequestBody requestBody = RequestBody.create(
                Client.MEDIA_TYPE_YAML,
                input
        );
        ServiceClient.WithErrorResponse<ACLPolicy> execute = rdTool.apiWithErrorResponseDowngradable(

                api -> func.apply(requestBody, api)
        );
        checkValidationError(
                output,
                rdTool.getClient(),
                execute,
                input.getAbsolutePath(),
                rdTool.getAppConfig().isAnsiEnabled()
        );
        return rdTool.getClient().checkError(execute);
    }

    private static void checkValidationError(
            CommandOutput output,
            final ServiceClient<RundeckApi> client,
            final ServiceClient.WithErrorResponse<ACLPolicy> errorResponse,
            final String filename, final boolean colorize
    )
            throws IOException
    {
        Response<ACLPolicy> response = errorResponse.getResponse();
        if (errorResponse.isError400()) {
            ACLPolicyValidation error = client.readError(
                    errorResponse.getErrorBody(),
                    ACLPolicyValidation.class,
                    Client.MEDIA_TYPE_JSON
            );
            if (null != error) {
                Optional<Map<String, Object>> validationData = Optional.ofNullable(error.toMap());
                validationData.ifPresent(map -> {
                    output.error("ACL Policy Validation failed for the file: ");
                    output.output(filename);
                    output.output(colorize ? Colorz.colorizeMapRecurse(map, ANSIColorOutput.Color.YELLOW) : map);
                });
                if (!validationData.isPresent() && "true".equals(error.errorString)) {
                    output.error("Invalid Request:");
                    //other error
                    client.reportApiError(error);
                }
            }
            throw new RequestFailed(String.format(
                    "ACLPolicy Validation failed: (error: %d %s)",
                    response.code(),
                    response.message()
            ), response.code(), response.message());
        }
    }


    @CommandLineInterface(application = "delete") interface Delete extends ProjectNameOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();

    }

    @Command(description = "Delete a project ACL definition")
    public void delete(Delete options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        apiCall(api -> api.deleteAclPolicy(project, options.getName()));
        output.info(String.format("Deleted ACL Policy for %s: %s", project, options.getName()));
    }
}
