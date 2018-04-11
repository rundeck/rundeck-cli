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
import org.rundeck.client.api.model.ScheduledJobItem;
import org.rundeck.client.tool.RdApp;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * scheduler subcommands
 */
@Command(description = "View scheduler information")
public class Scheduler extends AppCommand {
    public Scheduler(final RdApp client) {
        super(client);
    }


    @CommandLineInterface(application = "jobs") interface SchedulerJobs {


        @Option(shortName = "u",
                longName = "uuid",
                description = "Server UUID to query, or blank to select the target server")
        String getUuid();

        boolean isUuid();
    }

    @Command(description = "List jobs for the current target server, or a specified server.")
    public void jobs(SchedulerJobs options, CommandOutput output) throws IOException, InputError {
        List<ScheduledJobItem> jobInfo = apiCall(api -> {
            if (options.isUuid()) {
                return api.listSchedulerJobs(options.getUuid());
            } else {
                return api.listSchedulerJobs();
            }

        });
        output.output(jobInfo.stream().map(ScheduledJobItem::toMap).collect(Collectors.toList()));
    }
}
