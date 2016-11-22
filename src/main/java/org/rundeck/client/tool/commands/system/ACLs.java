package org.rundeck.client.tool.commands.system;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.RequestBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ACLPolicy;
import org.rundeck.client.api.model.ACLPolicyItem;
import org.rundeck.client.tool.commands.ApiCommand;
import org.rundeck.client.tool.commands.projects.ACLFileOptions;
import org.rundeck.client.tool.commands.projects.ACLNameOptions;
import org.rundeck.client.tool.options.ACLOutputOptions;
import org.rundeck.client.util.Client;

import java.io.IOException;
import java.util.function.Supplier;

import static org.rundeck.client.tool.commands.projects.ACLs.*;

/**
 * System ACLs
 */
@Command(description = "Manage System ACLs")
public class ACLs extends ApiCommand {
    public ACLs(final Supplier<Client<RundeckApi>> client) {
        super(client);
    }

    @CommandLineInterface(application = "list") interface ListOptions extends ACLOutputOptions {

    }
    @Command(description = "list system acls")
    public void list(ListOptions options, CommandOutput output) throws IOException {
        ACLPolicyItem items = getClient().checkError(getClient().getService()
                                                                .listSystemAcls());
        outputListResult(options, output, items, "system");
    }


    @CommandLineInterface(application = "get") interface Get extends ACLNameOptions {
    }

    @Command(description = "get a system ACL definition")
    public void get(Get options, CommandOutput output) throws IOException {
        ACLPolicy aclPolicy = getClient().checkError(getClient().getService()
                                                                .getSystemAclPolicy(options.getName()));
        outputPolicyResult(output, aclPolicy);
    }


    @CommandLineInterface(application = "upload") interface Put extends ACLNameOptions, ACLFileOptions {
    }

    @Command(description = "Upload a system ACL definition")
    public void upload(Put options, CommandOutput output)
            throws IOException, InputError
    {

        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> getClient().getService()
                                                 .updateSystemAclPolicy(options.getName(), body),
                getClient(), output
        );
        outputPolicyResult(output, aclPolicy);
    }

    @CommandLineInterface(application = "create") interface Create extends ACLNameOptions,
            ACLFileOptions
    {
    }

    @Command(description = "Create a system ACL definition")
    public void create(Create options, CommandOutput output) throws IOException, InputError {

        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> getClient().getService()
                                                 .createSystemAclPolicy(options.getName(), body),
                getClient(), output
        );
        outputPolicyResult(output, aclPolicy);
    }


    @CommandLineInterface(application = "delete") interface Delete extends ACLNameOptions {

    }

    @Command(description = "Delete a system ACL definition")
    public void delete(Delete options, CommandOutput output) throws IOException {
        getClient().checkError(getClient().getService().deleteSystemAclPolicy(options.getName()));
        output.output(String.format("Deleted System ACL Policy: %s", options.getName()));
    }
}
