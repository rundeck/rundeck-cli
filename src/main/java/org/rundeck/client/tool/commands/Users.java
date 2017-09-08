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
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.HasSubCommands;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectItem;
import org.rundeck.client.api.model.User;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.projects.*;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * user commands
 */
@Command(description = "Manage user information.")
public class Users extends AppCommand {
    public Users(final RdApp client) {
        super(client);
    }


    interface ProjectResultOptions extends ProjectListFormatOptions, VerboseOption {

    }

    @CommandLineInterface(application = "info") interface Info extends LoginNameOption {
    }

    @Command(
             description = "Get information of the same user or another user by Login name.")
    public void info(Info opts, CommandOutput output) throws IOException, InputError {

        User user = apiCall(api -> {
            if (opts.isLogin()) {
                return api.getUserInfo(opts.getLogin());
            } else {
                return api.getUserInfo();
            }

        });

        //output.info(String.format("User info [%s] succeeded.", opts.getLogin()));
        output.info("User profile:");

        output.output(String.format("Login: [%s]", user.getLogin()));
        if(user.hasFirstName()) {
            output.output(String.format("First Name: [%s]", user.getFirstName()));
        }
        if(user.hasLastName()) {
            output.output(String.format("Last Name: [%s]", user.getLastName()));
        }
        if(user.hasEmail()) {
            output.output(String.format("Email: [%s]", user.getEmail()));
        }
    }

}
