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

package org.rundeck.client.tool.commands.system;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.RequestBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ACLPolicy;
import org.rundeck.client.api.model.ACLPolicyItem;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.projects.ACLFileOptions;
import org.rundeck.client.tool.commands.projects.ACLNameOptions;
import org.rundeck.client.tool.options.ACLOutputOptions;
import org.rundeck.client.util.Client;

import java.io.IOException;

import static org.rundeck.client.tool.commands.projects.ACLs.*;

/**
 * System ACLs
 */
@Command(description = "Manage System ACLs")
public class ACLs extends AppCommand {
    public ACLs(final RdApp client) {
        super(client);
    }

    @CommandLineInterface(application = "list") interface ListOptions extends ACLOutputOptions {

    }
    @Command(description = "list system acls")
    public void list(ListOptions options, CommandOutput output) throws IOException, InputError {
        ACLPolicyItem items = apiCall(RundeckApi::listSystemAcls);
        outputListResult(options, output, items, "system");
    }


    @CommandLineInterface(application = "get") interface Get extends ACLNameOptions {
    }

    @Command(description = "get a system ACL definition")
    public void get(Get options, CommandOutput output) throws IOException, InputError {
        ACLPolicy aclPolicy = apiCall(api -> api.getSystemAclPolicy(options.getName()));
        outputPolicyResult(output, aclPolicy);
    }


    @CommandLineInterface(application = "upload") interface Put extends ACLNameOptions, ACLFileOptions {
    }

    @Command(description = "Upload a system ACL definition")
    public void upload(Put options, CommandOutput output)
            throws IOException, InputError
    {

        Client<RundeckApi> client = getClient();
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                            .updateSystemAclPolicy(options.getName(), body),
                client, output
        );
        outputPolicyResult(output, aclPolicy);
    }

    @CommandLineInterface(application = "create") interface Create extends ACLNameOptions,
            ACLFileOptions
    {
    }

    @Command(description = "Create a system ACL definition")
    public void create(Create options, CommandOutput output) throws IOException, InputError {

        Client<RundeckApi> client = getClient();
        ACLPolicy aclPolicy = performACLModify(
                options,
                (RequestBody body) -> client.getService()
                                            .createSystemAclPolicy(options.getName(), body),
                client, output
        );
        outputPolicyResult(output, aclPolicy);
    }


    @CommandLineInterface(application = "delete") interface Delete extends ACLNameOptions {

    }

    @Command(description = "Delete a system ACL definition")
    public void delete(Delete options, CommandOutput output) throws IOException, InputError {
        Client<RundeckApi> client = getClient();
        apiCall(api -> api.deleteSystemAclPolicy(options.getName()));
        output.output(String.format("Deleted System ACL Policy: %s", options.getName()));
    }
}
