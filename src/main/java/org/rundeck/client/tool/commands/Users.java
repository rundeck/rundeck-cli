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
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.User;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;

import java.io.IOException;
import java.util.List;
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

    @CommandLineInterface(application = "info") interface Info extends LoginNameOption,
            UserFormatOption
    {
    }

    @Command(description = "Get information of the same user or from another if 'user' is specified.")
    public void info(Info opts, CommandOutput output) throws IOException, InputError {
        requireApiVersion("users info", 21);
        User user = apiCall(api -> {
            if (opts.isLogin()) {
                return api.getUserInfo(opts.getLogin());
            } else {
                return api.getUserInfo();
            }

        });

        outputUserInfo(opts, output, user);
    }

    private void outputUserInfo(final UserFormatOption opts, final CommandOutput output, final User user) {
        final Function<User, ?> outformat;
        if (opts.isVerbose()) {
            output.output(user.toMap());
            return;
        }
        output.info("User profile:");
        if (opts.isOutputFormat()) {
            outformat = Format.formatter(opts.getOutputFormat(), User::toMap, "%", "");
            output.output(outformat.apply(user));
        } else {

            output.output(String.format("Login: [%s]", user.getLogin()));
            if (user.hasFirstName()) {
                output.output(String.format("First Name: [%s]", user.getFirstName()));
            }
            if (user.hasLastName()) {
                output.output(String.format("Last Name: [%s]", user.getLastName()));
            }
            if (user.hasEmail()) {
                output.output(String.format("Email: [%s]", user.getEmail()));
            }
        }
    }

    @CommandLineInterface(application = "edit") interface Edit extends LoginNameOption,
            UserFormatOption
    {
        @Option(shortName = "e", longName = "email", description = "user email")
        String getEmail();

        boolean isEmail();

        @Option(shortName = "n", longName = "name", description = "user first name")
        String getFirstName();

        boolean isFirstName();

        @Option(shortName = "l", longName = "last", description = "user last name")
        String getLastName();

        boolean isLastName();
    }

    @Command(description = "Edit information of the same user or another if 'user' is specified.")
    public void edit(Edit opts, CommandOutput output) throws IOException, InputError {
        requireApiVersion("users edit", 21);
        User u = new User();
        if (opts.isEmail()) {
            u.setEmail(opts.getEmail());
        }
        if (opts.isFirstName()) {
            u.setFirstName(opts.getFirstName());
        }
        if (opts.isLastName()) {
            u.setLastName(opts.getLastName());
        }

        User user = apiCall(api -> {
            if (opts.isLogin()) {
                return api.editUserInfo(opts.getLogin(), u);
            } else {
                return api.editUserInfo(u);
            }

        });

        outputUserInfo(opts, output, user);
    }

    @CommandLineInterface(application = "edit") interface ListOption extends UserFormatOption {
    }

    @Command(description = "Get the list of users.")
    public void list(ListOption options, CommandOutput output) throws IOException, InputError {
        requireApiVersion("users list", 21);
        List<User> users = apiCall(RundeckApi::listUsers);
        final Function<User, ?> outformat;
        if (options.isVerbose()) {
            output.output(users.stream().map(User::toMap).collect(Collectors.toList()));
            return;
        }
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), User::toMap, "%", "");
        } else {
            outformat = User::toBasicString;
        }
        output.info(String.format("%d Users:", users.size()));
        users.forEach(e -> output.output(outformat.apply(e)));
    }


}
