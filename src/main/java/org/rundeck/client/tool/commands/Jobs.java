package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.options.JobListOptions;
import org.rundeck.client.tool.options.JobLoadOptions;
import org.rundeck.client.tool.options.JobPurgeOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Util;
import retrofit2.Call;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by greg on 3/28/16.
 */
@Command(description = "List and manage Jobs.")
public class Jobs extends ApiCommand {

    public static final String UUID_REMOVE = "remove";
    public static final String UUID_PRESERVE = "preserve";

    public Jobs(final Client<RundeckApi> client) {
        super(client);
    }

    @CommandLineInterface(application = "purge") interface Purge extends JobPurgeOptions, ListOpts {
    }

    @Command(description = "Delete jobs matching the query parameters. Optionally save the definitions to a file " +
                           "before deleting from the server. " +
                           "--idlist/-i, or --job/-j or --group/-g Options are required.")
    public boolean purge(Purge options, CommandOutput output) throws IOException {

        //if id,idlist specified, use directly
        //otherwise query for the list and assemble the ids

        List<String> ids = new ArrayList<>();
        if (options.isIdlist()) {
            ids = Arrays.asList(options.getIdlist().split("\\s*,\\s*"));
        } else {
            if (!options.isJob() && !options.isGroup()) {
                throw new IllegalArgumentException("must specify -i, or -j/-g to specify jobs to delete.");
            }
            Call<List<JobItem>> listCall;
            listCall = client.getService().listJobs(
                    options.getProject(),
                    options.getJob(),
                    options.getGroup()
            );
            List<JobItem> body = client.checkError(listCall);
            for (JobItem jobItem : body) {
                ids.add(jobItem.getId());
            }
        }

        if (options.isFile()) {
            list(options, output);
        }

        DeleteJobsResult deletedJobs = client.checkError(client.getService().deleteJobs(ids));

        if (deletedJobs.isAllsuccessful()) {
            output.output(String.format("%d Jobs were deleted%n", deletedJobs.getRequestCount()));
            return true;
        }
        output.output(String.format("Failed to delete %d Jobs%n", deletedJobs.getFailed().size()));
        for (DeleteJob deleteJob : deletedJobs.getFailed()) {
            output.output(String.format("* " + deleteJob.toBasicString()));
        }
        return false;
    }

    @CommandLineInterface(application = "load") interface Load extends JobLoadOptions {
    }

    @Command(description = "Load Job definitions from a file in XML or YAML format.")
    public boolean load(Load options, CommandOutput output) throws IOException {
        if (!options.isFile()) {
            throw new IllegalArgumentException("-f is required");
        }
        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new IllegalArgumentException(String.format("File is not readable or does not exist: %s", input));
        }

        RequestBody requestBody = RequestBody.create(
                "xml".equals(options.getFormat()) ? Client.MEDIA_TYPE_XML : Client.MEDIA_TYPE_YAML,
                input
        );

        Call<ImportResult> importResultCall = client.getService().loadJobs(
                options.getProject(),
                requestBody,
                options.getFormat(),
                options.getDuplicate(),
                options.isRemoveUuids() ? UUID_REMOVE : UUID_PRESERVE
        );
        ImportResult importResult = client.checkError(importResultCall);

        List<JobLoadItem> failed = importResult.getFailed();

        printLoadResult(importResult.getSucceeded(), "Succeeded", output);
        printLoadResult(importResult.getSkipped(), "Skipped", output);
        printLoadResult(failed, "Failed", output);

        return failed == null || failed.size() == 0;
    }

    private static void printLoadResult(final List<JobLoadItem> list, final String title, CommandOutput output) {
        if (null != list && list.size() > 0) {
            output.output(String.format("%d Jobs " + title + ":%n", list != null ? list.size() : 0));
            for (JobLoadItem jobLoadItem : list) {
                output.output(String.format("* %s%n", jobLoadItem.toBasicString()));
            }
        }
    }

    @CommandLineInterface(application = "list") interface ListOpts extends JobListOptions {
    }

    @Command(description = "List jobs found in a project, or download Job definitions (-f).")
    public void list(ListOpts options, CommandOutput output) throws IOException {
        if (options.isFile()) {
            //write response to file instead of parsing it
            Call<ResponseBody> responseCall;
            if (options.isIdlist()) {
                responseCall = client.getService().exportJobs(
                        options.getProject(),
                        options.getIdlist(),
                        options.getFormat()
                );
            } else {
                responseCall = client.getService().exportJobs(
                        options.getProject(),
                        options.getJob(),
                        options.getGroup(),
                        options.getFormat()
                );
            }
            ResponseBody body = client.checkError(responseCall);
            if ((!"yaml".equals(options.getFormat()) ||
                 !Client.hasAnyMediaType(body, Client.MEDIA_TYPE_YAML, Client.MEDIA_TYPE_TEXT_YAML)) &&
                !Client.hasAnyMediaType(body, Client.MEDIA_TYPE_XML, Client.MEDIA_TYPE_TEXT_XML)) {

                throw new IllegalStateException("Unexpected response format: " + body.contentType());
            }
            InputStream inputStream = body.byteStream();
            try (FileOutputStream out = new FileOutputStream(options.getFile())) {
                long total = Util.copyStream(inputStream, out);
                output.output(String.format(
                        "Wrote %d bytes of %s to file %s%n",
                        total,
                        body.contentType(),
                        options.getFile()
                ));
            }
        } else {
            Call<List<JobItem>> listCall;
            if (options.isIdlist()) {
                listCall = client.getService().listJobs(options.getProject(), options.getIdlist());
            } else {
                listCall = client.getService().listJobs(
                        options.getProject(),
                        options.getJob(),
                        options.getGroup()
                );
            }
            List<JobItem> body = client.checkError(listCall);
            output.output(String.format("%d Jobs in project %s%n", body.size(), options.getProject()));
            for (JobItem jobItem : body) {
                output.output("* " + jobItem.toBasicString());
            }
        }
    }

    @CommandLineInterface(application = "info") interface InfoOpts {

        @Option(shortName = "i", longName = "id", description = "Job ID")
        String getId();
    }

    @Command(description = "Get info about a Job by ID (API v18)")
    public void info(InfoOpts options, CommandOutput output) throws IOException {
        ScheduledJobItem body = client.checkError(client.getService().getJobInfo(options.getId()));
        output.output(body.toMap());
    }

    /**
     * Split a job group/name into group then name parts
     *
     * @param job job group + name
     *
     * @return [job group (or null), name]
     */
    public static String[] splitJobNameParts(final String job) {
        if (!job.contains("/")) {
            return new String[]{null, job};
        }
        int i = job.lastIndexOf("/");
        String group = job.substring(0, i);
        String name = job.substring(i + 1);
        if ("".equals(group.trim())) {
            group = null;
        }
        return new String[]{group, name};

    }
}
