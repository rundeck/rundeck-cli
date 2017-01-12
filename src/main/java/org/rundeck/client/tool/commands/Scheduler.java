package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.model.ScheduledJobItem;
import org.rundeck.client.tool.RdApp;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by greg on 7/8/16.
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
