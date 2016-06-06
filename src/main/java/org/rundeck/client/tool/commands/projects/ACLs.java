package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import okhttp3.RequestBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ACLPolicy;
import org.rundeck.client.api.model.ACLPolicyItem;
import org.rundeck.client.tool.commands.ApiCommand;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import org.rundeck.util.toolbelt.Command;
import org.rundeck.util.toolbelt.CommandOutput;
import org.rundeck.util.toolbelt.SubCommand;
import retrofit2.Call;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * Created by greg on 6/6/16.
 */
@SubCommand()
public class ACLs extends ApiCommand {
    public ACLs(final Client<RundeckApi> client) {
        super(client);
    }

    @Command(description = "list project acls")
    public void list(ProjectNameOptions options, CommandOutput output) throws IOException {
        ACLPolicyItem ACLPolicyItems = client.checkError(client.getService()
                                                                         .listAcls(options.getProject()));
        output.output(String.format(
                "%d ACL Policy items for project %s",
                ACLPolicyItems.getResources().size(),
                options.getProject()
        ));
        ACLPolicyItems.getResources().forEach(a -> output.output(String.format("* %s", a.getPath())));
    }

    @CommandLineInterface(application = "get") interface Get extends ProjectNameOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();
    }

    @Command(description = "get a project ACL definition")
    public void get(Get options, CommandOutput output) throws IOException {
        ACLPolicy aclPolicy = client.checkError(client.getService()
                                                                .getAclPolicy(options.getProject(), options.getName()));
        output.output(aclPolicy.getContents());
    }

    interface FileOptions {

        @Option(shortName = "f", longName = "file", description = "ACLPolicy file to upload")
        File getFile();
    }

    @CommandLineInterface(application = "upload") interface Put extends ProjectNameOptions, FileOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();

    }

    @Command(description = "Upload a project ACL definition")
    public void upload(Put options, CommandOutput output) throws IOException {
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                                 .updateAclPolicy(options.getProject(), options.getName(), body)
        );
        output.output(aclPolicy.getContents());
    }

    @CommandLineInterface(application = "create") interface Create extends ProjectNameOptions, FileOptions {
        @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
        String getName();

    }

    @Command(description = "Create a project ACL definition")
    public void create(Create options, CommandOutput output) throws IOException {

        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                                 .createAclPolicy(options.getProject(), options.getName(), body)
        );
        output.output(aclPolicy.getContents());
    }

    private ACLPolicy performACLModify(final FileOptions options, Function<RequestBody, Call<ACLPolicy>> func)
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
        return client.checkError(func.apply(requestBody));

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
