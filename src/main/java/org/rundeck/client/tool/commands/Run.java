package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
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
import java.util.function.Supplier;

import static org.rundeck.client.tool.options.OptionUtil.projectOrEnv;

/**
 * Created by greg on 5/20/16.
 */
@Command(description = "Run a Job.")
public class Run extends ApiCommand {
    public Run(final HasClient client) {
        super(client);
    }

    @Command(isDefault = true, isSolo = true)
    public boolean run(RunBaseOptions options, CommandOutput out) throws IOException, InputError {
        String jobId;
        if (options.isJob()) {
            if (!options.isProject()) {
                throw new InputError("-p project is required with -j");
            }
            String job = options.getJob();
            String[] parts = Jobs.splitJobNameParts(job);
            String project = projectOrEnv(options);
            List<JobItem> jobItems = apiCall(api -> api.listJobs(
                    project,
                    null,
                    null,
                    parts[1],
                    parts[0]
            ));
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
            out.info(String.format("Found matching job: %s%n", jobItem.toBasicString()));
            jobId = jobItem.getId();
        } else if (options.isId()) {
            jobId = options.getId();
        } else {
            throw new InputError("-j job or -i id is required");

        }
        Call<Execution> executionListCall;
        if (getClient().getApiVersion() >= 18) {
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
                throw new InputError(
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
                    throw new InputError("-@/--at date format is not valid", e);
                }
            }
            executionListCall = getClient().getService().runJob(jobId, request);
        } else {
            executionListCall = getClient().getService().runJob(
                    jobId,
                    Quoting.joinStringQuoted(options.getCommandString()),
                    options.getLoglevel(),
                    options.getFilter(),
                    options.getUser()
            );
        }
        Execution execution = getClient().checkError(executionListCall);
        out.info(String.format("Execution started: %s%n", execution.toBasicString()));

        return Executions.maybeFollow(getClient(), options, execution.getId(), out);
    }
}
