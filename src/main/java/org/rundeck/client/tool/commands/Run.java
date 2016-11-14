package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.JobRun;
import org.rundeck.client.tool.options.RunBaseOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Quoting;
import retrofit2.Call;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Call<Execution> executionListCall;
        if (client.getApiVersion() >= 18) {
            JobRun request = new JobRun();
            request.setLoglevel(options.getLoglevel());
            request.setFilter(options.getFilter());
            request.setAsUser(options.getUser());
            List<String> commandString = options.getCommandString();
            Map<String, String> jobopts = new HashMap<>();
            String key = null;
            if (null != commandString) {
                for (String s : commandString) {
                    if (key == null && s.startsWith("-")) {
                        key = s.substring(1);
                    } else if (key != null) {
                        jobopts.put(key, s);
                        key = null;
                    }
                }
            }
            if (key != null) {
                throw new IllegalArgumentException(
                        String.format(
                                "Incorrect job options, expected: \"-%s value\", but saw only \"-%s\"",
                                key,
                                key
                        ));
            }

            request.setOptions(jobopts);
            if (null != options.getRunAtDate()) {
                try {
                    request.setRunAtTime(options.getRunAtDate().toDate("yyyy-MM-dd'T'HH:mm:ssXX"));
                } catch (ParseException e) {
                    throw new IllegalArgumentException("-@/--at date format is not valid", e);
                }
            }
            executionListCall = client.getService().runJob(jobId, request);
        } else {
            executionListCall = client.getService().runJob(
                    jobId,
                    Quoting.joinStringQuoted(options.getCommandString()),
                    options.getLoglevel(),
                    options.getFilter(),
                    options.getUser()
            );
        }
        Execution execution = client.checkError(executionListCall);
        out.output(String.format("Execution started: %s%n", execution.toBasicString()));

        return Executions.maybeFollow(client, options, execution.getId(), out);
    }
}
