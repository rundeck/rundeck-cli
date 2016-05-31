package org.rundeck.client.tool.commands;

import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.util.toolbelt.Command;
import org.rundeck.util.toolbelt.CommandOutput;
import org.rundeck.util.toolbelt.CommandRunFailure;
import org.rundeck.client.tool.options.RunBaseOptions;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
@Command(description = "Run a Job.")
public class Run extends ApiCommand {
    public Run(final Client<RundeckApi> client) {
        super(client);
    }

    @Command(isDefault = true, isSolo = true)
    public boolean run(RunBaseOptions options, CommandOutput out) throws IOException {
        String jobId;
        if (options.isJob()) {
            if (!options.isProject()) {
                throw new IllegalArgumentException("-p project is required with -j");
            }
            String job = options.getJob();
            String[] parts = Jobs.splitJobNameParts(job);
            Call<List<JobItem>> listCall = client.getService().listJobs(options.getProject(), parts[1], parts[0]);
            List<JobItem> jobItems = client.checkError(listCall);
            if (jobItems.size() != 1) {
                out.error(String.format("Could not find a unique job with name: %s%n", job));
                if (jobItems.size() > 0) {

                    out.error(String.format("Found %d matching jobs:%n", jobItems.size()));
                    for (JobItem jobItem : jobItems) {
                        out.error(String.format("* %s%n", jobItem.toBasicString()));

                    }
                } else {
                    out.error("Found 0 matching jobs.");
                }
                return false;
            }
            JobItem jobItem = jobItems.get(0);
            out.output(String.format("Found matching job: %s%n", jobItem.toBasicString()));
            jobId = jobItem.getId();
        } else if (options.isId()) {
            jobId = options.getId();
        } else {
            throw new IllegalArgumentException("-j job or -i id is required");

        }
        Call<Execution> executionListCall = client.getService().runJob(
                jobId,
                Adhoc.joinString(options.getCommandString()),
                options.getLoglevel(),
                options.getFilter()
        );
        Execution execution = client.checkError(executionListCall);
        out.output(String.format("Execution started: %s%n", execution.toBasicString()));

        return Executions.maybeFollow(client, options, execution.getId(), out);
    }
}
