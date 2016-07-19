package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.ANSIColorOutput;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import okhttp3.RequestBody;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ACLPolicy;
import org.rundeck.client.api.model.ACLPolicyItem;
import org.rundeck.client.api.model.ACLPolicyValidation;
import org.rundeck.client.tool.commands.ApiCommand;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Colorz;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * Created by greg on 6/6/16.
 */
@Command(description = "Manage Project ACLs")
public class ACLs extends ApiCommand {
    public ACLs(final Client<RundeckApi> client) {
        super(client);
    }

    @Command(description = "list project acls")
    public void list(ProjectNameOptions options, CommandOutput output) throws IOException {
        ACLPolicyItem items = client.checkError(client.getService()
                                                      .listAcls(options.getProject()));
        outputListResult(output, items, String.format("project %s", options.getProject()));
    }

    public static void outputListResult(
            final CommandOutput output,
            final ACLPolicyItem ACLPolicyItems,
            final String ident
    )
    {
        output.output(String.format(
                "%d ACL Policy items for %s",
                ACLPolicyItems.getResources().size(),
                ident
        ));
        ACLPolicyItems.getResources().forEach(a -> output.output(String.format("* %s", a.getPath())));
    }

    @CommandLineInterface(application = "get") interface Get extends ACLNameOptions, ProjectNameOptions {
    }

    @Command(description = "get a project ACL definition")
    public void get(Get options, CommandOutput output) throws IOException {
        ACLPolicy aclPolicy = client.checkError(client.getService()
                                                                .getAclPolicy(options.getProject(), options.getName()));
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
    public void upload(Put options, CommandOutput output) throws IOException {
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                            .updateAclPolicy(options.getProject(), options.getName(), body), client,
                output
        );
        outputPolicyResult(output, aclPolicy);
    }

    @CommandLineInterface(application = "create") interface Create extends ProjectNameOptions, ACLFileOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();

    }

    @Command(description = "Create a project ACL definition")
    public void create(Create options, CommandOutput output) throws IOException {

        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                            .createAclPolicy(options.getProject(), options.getName(), body),
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
     * @throws IOException
     */
    public static ACLPolicy performACLModify(
            final ACLFileOptions options,
            Function<RequestBody, Call<ACLPolicy>> func,
            final Client<RundeckApi> client,
            final CommandOutput output
    )
            throws IOException
    {

        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new IllegalArgumentException(String.format("File is not readable or does not exist: %s", input));
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
            final Client<RundeckApi> client,
            final Response<ACLPolicy> response, final String filename
    )
            throws IOException
    {

        if (!response.isSuccessful()) {

            if (response.code() == 400) {
                ACLPolicyValidation error = client.readError(
                        response,
                        ACLPolicyValidation.class,
                        Client.MEDIA_TYPE_JSON
                );
                if (null != error) {
                    output.error("ACL Policy Validation failed for the file: ");
                    output.output(filename + "\n");

                    output.output(Colorz.colorizeMapRecurse(error.toMap(), ANSIColorOutput.Color.YELLOW));
                }
                throw new RequestFailed(String.format(
                        "ACLPolicy Validation failed: (error: %d %s)",
                        response.code(),
                        response.message()

                ), response.code(), response.message());
            }
        }
    }


    @CommandLineInterface(application = "delete") interface Delete extends ProjectNameOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();

    }

    @Command(description = "Delete a project ACL definition")
    public void delete(Delete options, CommandOutput output) throws IOException {
        client.checkError(client.getService().deleteAclPolicy(options.getProject(), options.getName()));
        output.output(String.format("Deleted ACL Policy for %s: %s", options.getProject(), options.getName()));
    }
}
