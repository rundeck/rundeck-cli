package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.ScheduledJobItem;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.tool.options.ProjectCreateOptions;
import org.rundeck.client.util.Client;
import org.rundeck.util.toolbelt.Command;
import org.rundeck.util.toolbelt.CommandOutput;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by greg on 7/8/16.
 */
@Command(description = "View scheduler information")
public class Scheduler extends ApiCommand {
    public Scheduler(final Client<RundeckApi> client) {
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
    public void jobs(SchedulerJobs options, CommandOutput output) throws IOException {
        Call<List<ScheduledJobItem>> call;
        if (options.isUuid()) {
            call = client.getService().listSchedulerJobs(options.getUuid());
        } else {
            call = client.getService().listSchedulerJobs();
        }
        List<ScheduledJobItem> jobInfo = client.checkError(call);
        output.output(jobInfo.stream().map(ScheduledJobItem::toMap).collect(Collectors.toList()));
    }
}
