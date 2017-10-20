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
import com.simplifyops.toolbelt.ANSIColorOutput;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
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
import org.rundeck.client.util.Colorz;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.ServiceClient;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
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
        ServiceClient<RundeckApi> client = getClient();
        String project = projectOrEnv(options);
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                            .updateAclPolicy(project, options.getName(), body),
                client,
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

        ServiceClient<RundeckApi> client = getClient();
        String project = projectOrEnv(options);
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                        .createAclPolicy(project, options.getName(), body),
                client, output
        );
        outputPolicyResult(output, aclPolicy);
    }

    /**
     * Upload a file to create/modify an ACLPolicy
     *
     * @param options file options
     * @param func    create the request
     * @param client  api client
     * @param output  output
     *
     * @return result policy
     *
     */
    public static ACLPolicy performACLModify(
            final ACLFileOptions options,
            Function<RequestBody, Call<ACLPolicy>> func,
            final ServiceClient<RundeckApi> client,
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
        Call<ACLPolicy> apply = func.apply(requestBody);
        Response<ACLPolicy> execute = apply.execute();
        checkValidationError(output, client, execute, input.getAbsolutePath());
        return client.checkError(execute);
    }

    private static void checkValidationError(
            CommandOutput output,
            final ServiceClient<RundeckApi> client,
            final Response<ACLPolicy> response, final String filename
    )
            throws IOException
    {

        if (!response.isSuccessful() && response.code() == 400) {
            ACLPolicyValidation error = client.readError(
                    response,
                    ACLPolicyValidation.class,
                    Client.MEDIA_TYPE_JSON
            );
            if (null != error) {
                Optional<Map<String, Object>> validationData = Optional.ofNullable(error.toMap());
                validationData.ifPresent(map -> {
                    output.error("ACL Policy Validation failed for the file: ");
                    output.output(filename + "\n");
                    output.output(Colorz.colorizeMapRecurse(map, ANSIColorOutput.Color.YELLOW));
                });
                if (!validationData.isPresent() && "true".equals(error.error)) {
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
