package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ScheduledJobItem;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by greg on 7/8/16.
 */
@Command(description = "View scheduler information")
public class Scheduler extends ApiCommand {
    public Scheduler(final Supplier<Client<RundeckApi>> client) {
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
            call = getClient().getService().listSchedulerJobs(options.getUuid());
        } else {
            call = getClient().getService().listSchedulerJobs();
        }
        List<ScheduledJobItem> jobInfo = getClient().checkError(call);
        output.output(jobInfo.stream().map(ScheduledJobItem::toMap).collect(Collectors.toList()));
    }
}
