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

import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.ACLOutputFormatOption;
import org.rundeck.client.tool.InputError;
import okhttp3.RequestBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ACLPolicy;
import org.rundeck.client.api.model.ACLPolicyItem;
import picocli.CommandLine;

import java.io.IOException;

import static org.rundeck.client.tool.commands.projects.ACLs.*;

/**
 * System ACLs
 */
@CommandLine.Command(description = "Manage System ACLs", name = "acls")
public class ACLs extends BaseCommand {


    @CommandLine.Command(description = "list system acls")
    public void list(@CommandLine.Mixin ACLOutputFormatOption options) throws IOException, InputError {
        ACLPolicyItem items = apiCall(RundeckApi::listSystemAcls);
        outputListResult(options, getRdOutput(), items, "system");
    }


    @CommandLine.Command(description = "get a system ACL definition")
    public void get(@CommandLine.Mixin ACLNameRequiredOptions options) throws IOException, InputError {
        ACLPolicy aclPolicy = apiCall(api -> api.getSystemAclPolicy(options.getName()));
        outputPolicyResult(getRdOutput(), aclPolicy);
    }

    @CommandLine.Command(description = "Update an existing system ACL definition. [@|red DEPRECATED|@: use @|bold update|@]", hidden = true)
    public void upload(@CommandLine.Mixin ACLNameRequiredOptions aclNameOptions, @CommandLine.Mixin ACLFileOptions aclFileOptions)
            throws IOException, InputError {
        getRdOutput().warning("rd system acls upload command is deprecated, use: rd system acls update");
        update(aclNameOptions, aclFileOptions);
    }

    @CommandLine.Command(description = "Update an existing system ACL definition")
    public void update(@CommandLine.Mixin ACLNameRequiredOptions aclNameOptions, @CommandLine.Mixin ACLFileOptions aclFileOptions)
            throws IOException, InputError {
        ACLPolicy aclPolicy = performACLModify(
                aclFileOptions,
                (RequestBody body, RundeckApi api) -> api.updateSystemAclPolicy(aclNameOptions.getName(), body),
                getRdTool(),
                getRdOutput()
        );
        outputPolicyResult(getRdOutput(), aclPolicy);
    }


    @CommandLine.Command(description = "Create a system ACL definition")
    public void create(@CommandLine.Mixin ACLNameRequiredOptions aclNameOptions, @CommandLine.Mixin ACLFileOptions aclFileOptions) throws IOException, InputError {
        ACLPolicy aclPolicy = performACLModify(
                aclFileOptions,
                (RequestBody body, RundeckApi api) -> api.createSystemAclPolicy(aclNameOptions.getName(), body),
                getRdTool(),
                getRdOutput()
        );
        outputPolicyResult(getRdOutput(), aclPolicy);
    }


    @CommandLine.Command(description = "Delete a system ACL definition")
    public void delete(@CommandLine.Mixin ACLNameRequiredOptions aclNameOptions) throws IOException, InputError {
        apiCall(api -> api.deleteSystemAclPolicy(aclNameOptions.getName()));
        getRdOutput().output(String.format("Deleted System ACL Policy: %s", aclNameOptions.getName()));
    }
}
