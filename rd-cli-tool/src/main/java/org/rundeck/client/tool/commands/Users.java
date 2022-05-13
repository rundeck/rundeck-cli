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


import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.extension.BaseCommand;
import picocli.CommandLine;
import org.rundeck.client.api.model.RoleList;


import org.rundeck.client.tool.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.User;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * user commands
 */
@CommandLine.Command(description = "Manage user information.", name = "users")
public class Users extends BaseCommand {


    @CommandLine.Command(description = "Get information of the same user or from another if 'user' is specified.")
    public void info(@CommandLine.Mixin LoginNameOption nameOption, @CommandLine.Mixin final UserFormatOption opts) throws IOException, InputError {
        getRdTool().requireApiVersion("users info", 21);
        User user = apiCall(api -> {
            if (nameOption.isLogin()) {
                return api.getUserInfo(nameOption.getLogin());
            } else {
                return api.getUserInfo();
            }

        });

        outputUserInfo(user, opts);
    }

    private void outputUserInfo(final User user, OutputFormat format) {
        final Function<User, ?> outformat;
        CommandOutput output = getRdOutput();
        if (format.isVerbose()) {
            output.output(user.toMap());
            return;
        }
        output.info("User profile:");
        if (format.isOutputFormat()) {
            outformat = Format.formatter(format.getOutputFormat(), User::toMap, "%", "");
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

    @Getter @Setter
    static class Edit extends LoginNameOption {
        @CommandLine.Option(names = {"-e", "--email"}, description = "user email")
        private String email;

        boolean isEmail() {
            return email != null;
        }

        @CommandLine.Option(names = {"-n", "--name"}, description = "user first name")
        private String firstName;

        boolean isFirstName() {
            return firstName != null;
        }

        @CommandLine.Option(names = {"-l", "--last"}, description = "user last name")
        String lastName;

        boolean isLastName() {
            return lastName != null;
        }
    }

    @CommandLine.Command(description = "Edit information of the same user or another if 'user' is specified.")
    public void edit(@CommandLine.Mixin Edit opts, @CommandLine.Mixin final UserFormatOption formatOption) throws IOException, InputError {
        getRdTool().requireApiVersion("users edit", 21);
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

        outputUserInfo(user, formatOption);
    }


    @CommandLine.Command(description = "Get the list of users.")
    public void list(@CommandLine.Mixin final UserFormatOption formatOption) throws IOException, InputError {
        getRdTool().requireApiVersion("users list", 21);
        List<User> users = apiCall(RundeckApi::listUsers);
        final Function<User, ?> outformat;
        if (formatOption.isVerbose()) {
            getRdOutput().output(users.stream().map(User::toMap).collect(Collectors.toList()));
            return;
        }
        if (formatOption.isOutputFormat()) {
            outformat = Format.formatter(formatOption.getOutputFormat(), User::toMap, "%", "");
        } else {
            outformat = User::toBasicString;
        }
        getRdOutput().info(String.format("%d Users:", users.size()));
        users.forEach(e -> getRdOutput().output(outformat.apply(e)));
    }

    @CommandLine.Command(description = "Get the list of roles for the current user.")
    public void roles() throws IOException, InputError {
        getRdTool().requireApiVersion("user roles", 30);
        RoleList roleList = apiCall(RundeckApi::listRoles);
        getRdOutput().info(String.format("%d Roles:", roleList.getRoles().size()));
        roleList.getRoles().forEach(e -> getRdOutput().output(e));
    }


}
