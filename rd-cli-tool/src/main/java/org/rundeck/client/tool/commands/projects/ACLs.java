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

import lombok.Data;
import okhttp3.RequestBody;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ACLPolicy;
import org.rundeck.client.api.model.ACLPolicyItem;
import org.rundeck.client.api.model.ACLPolicyValidation;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.tool.options.ACLOutputFormatOption;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.util.Colorz;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.ServiceClient;
import picocli.CommandLine;
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
@CommandLine.Command(description = "Manage Project ACLs", name = "acls")
public class ACLs extends BaseCommand {
    @CommandLine.Mixin
    ProjectNameOptions projectNameOptions;

    private String getProjectName() throws InputError {
        return getRdTool().projectOrEnv(projectNameOptions);
    }

    @CommandLine.Command(description = "list project acls")
    public void list(@CommandLine.Mixin ACLOutputFormatOption outputOptions) throws IOException, InputError {
        String project = getProjectName();
        ACLPolicyItem items = apiCall(api -> api.listAcls(project));
        outputListResult(outputOptions, getRdOutput(), items, String.format("project %s", project));
    }

    public static void outputListResult(
            final ACLOutputFormatOption options,
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
        } else if (options.getOutputFormat() != null) {
            outformat = Format.formatter(options.getOutputFormat(), ACLPolicyItem::toMap, "%", "");
        } else {
            outformat = ACLPolicyItem::getPath;

        }
        output.output(aclList.getResources()
                .stream()
                .map(outformat)
                .collect(Collectors.toList()));
    }

    @Data
    public static class ACLNameOptions {
        @CommandLine.Option(names = {"-n", "--name"}, description = "name of the aclpolicy file")
        String name;
    }

    @Data
    public static class ACLNameRequiredOptions {
        @CommandLine.Option(names = {"-n", "--name"}, description = "name of the aclpolicy file", required = true)
        String name;
    }

    @Data
    public static class ACLFileOptions {
        @CommandLine.Option(names = {"-f", "--file"}, description = "ACLPolicy file to upload", required = true)
        File file;
    }

    @CommandLine.Command(description = "get a project ACL definition")
    public void get(@CommandLine.Mixin ACLNameRequiredOptions aclNameOptions, CommandOutput output) throws IOException, InputError {
        String project = getProjectName();
        ACLPolicy aclPolicy = apiCall(api -> api.getAclPolicy(project, aclNameOptions.name));
        outputPolicyResult(output, aclPolicy);
    }

    public static void outputPolicyResult(final CommandOutput output, final ACLPolicy aclPolicy) {
        output.output(aclPolicy.getContents());
    }


    @CommandLine.Command(description = "Upload a project ACL definition")
    public void upload(@CommandLine.Mixin ACLNameOptions nameOptions, @CommandLine.Mixin ACLFileOptions fileOptions) throws IOException, InputError {
        String project = getProjectName();
        ACLPolicy aclPolicy = performACLModify(
                fileOptions,
                (RequestBody body, RundeckApi api) -> api.updateAclPolicy(project, nameOptions.getName(), body),
                getRdTool(),
                getRdOutput()
        );
        outputPolicyResult(getRdOutput(), aclPolicy);
    }

    @CommandLine.Command(description = "Create a project ACL definition")
    public void create(@CommandLine.Mixin ACLNameRequiredOptions nameOptions, @CommandLine.Mixin ACLFileOptions fileOptions) throws IOException, InputError {
        String project = getProjectName();
        ACLPolicy aclPolicy = performACLModify(
                fileOptions,
                (RequestBody body, RundeckApi api) -> api.createAclPolicy(project, nameOptions.getName(), body),
                getRdTool(),
                getRdOutput()
        );
        outputPolicyResult(getRdOutput(), aclPolicy);
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
                    output.output(colorize ? Colorz.colorizeMapRecurse(map, "yellow") : map);
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


    @CommandLine.Command(description = "Delete a project ACL definition")
    public void delete(@CommandLine.Mixin ACLNameRequiredOptions options) throws IOException, InputError {
        String project = getProjectName();
        apiCall(api -> api.deleteAclPolicy(project, options.getName()));
        getRdOutput().info(String.format("Deleted ACL Policy for %s: %s", project, options.getName()));
    }
}
