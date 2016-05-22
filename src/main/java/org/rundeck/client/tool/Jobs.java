package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.options.JobListOptions;
import org.rundeck.client.tool.options.JobLoadOptions;
import org.rundeck.client.tool.options.JobPurgeOptions;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by greg on 3/28/16.
 */
public class Jobs {

    public static final String UUID_REMOVE = "remove";
    public static final String UUID_PRESERVE = "preserve";

    public static void main(String[] args) throws IOException {
        Client<RundeckApi> client = App.createClient();
        String[] actions = new String[]{"list", "load", "purge"};
        boolean success = true;
        if ("list".equals(args[0])) {
            list(App.tail(args), client);
        } else if ("load".equals(args[0])) {
            success = load(App.tail(args), client);
        } else if ("purge".equals(args[0])) {
            success = purge(App.tail(args), client);
        } else {

            throw new IllegalArgumentException(String.format("Unrecognized action: %s, expected one of %s", args[0],
                                                             Arrays.asList(actions)
            ));
        }
        if (!success) {
            System.exit(2);
        }
    }

    private static boolean purge(final String[] args, final Client<RundeckApi> client) throws IOException {
        JobPurgeOptions options = CliFactory.parseArguments(JobPurgeOptions.class, args);

        //if id,idlist specified, use directly
        //otherwise query for the list and assemble the ids

        //TODO: if file specified, write to file

        List<String> ids = new ArrayList<>();
        if (options.isIdlist()) {
            ids = Arrays.asList(options.getIdlist().split("\\s*,\\s*"));
        } else {
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

        DeleteJobsResult deletedJobs = client.checkError(client.getService().deleteJobs(ids));

        if (deletedJobs.isAllsuccessful()) {
            System.out.printf("%d Jobs were deleted", deletedJobs.getRequestCount());
            return true;
        }
        System.out.printf("Failed to delete %d Jobs%n", deletedJobs.getFailed().size());
        for (DeleteJob deleteJob : deletedJobs.getFailed()) {
            System.out.println("* " + deleteJob.toBasicString());
        }
        return false;
    }

    private static boolean load(final String[] args, final Client<RundeckApi> client) throws IOException {
        JobLoadOptions options = CliFactory.parseArguments(JobLoadOptions.class, args);
        if (!options.isFile()) {
            throw new IllegalArgumentException("-f is required");
        }
        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new IllegalArgumentException(String.format("File is not readable or does not exist: %s", input));
        }

        RequestBody requestBody = RequestBody.create(
                "xml".equals(options.getFormat()) ? App.MEDIA_TYPE_XML : App.MEDIA_TYPE_YAML,
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

        printLoadResult(importResult.getSucceeded(), "Succeeded");
        printLoadResult(importResult.getSkipped(), "Skipped");
        printLoadResult(failed, "Failed");

        return failed == null || failed.size() == 0;
    }

    private static void printLoadResult(final List<JobLoadItem> list, final String title) {
        if (null != list && list.size() > 0) {
            System.out.printf("%d Jobs " + title + ":%n", list != null ? list.size() : 0);
            for (JobLoadItem jobLoadItem : list) {
                System.out.printf("* %s%n", jobLoadItem.toBasicString());
            }
        }
    }

    private static void list(final String[] args, final Client<RundeckApi> client) throws IOException {
        JobListOptions options = CliFactory.parseArguments(JobListOptions.class, args);


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
                 !App.hasAnyMediaType(body, App.MEDIA_TYPE_YAML, App.MEDIA_TYPE_TEXT_YAML)) &&
                !App.hasAnyMediaType(body, App.MEDIA_TYPE_XML, App.MEDIA_TYPE_TEXT_XML)) {

                throw new IllegalStateException("Unexpected response format: " + body.contentType());
            }
            InputStream inputStream = body.byteStream();
            long total = 0;
            try (FileOutputStream out = new FileOutputStream(options.getFile())) {
                byte[] buff = new byte[10240];
                int count = inputStream.read(buff);
                while (count > 0) {
                    out.write(buff, 0, count);
                    total += count;
                    count = inputStream.read(buff);
                }
            }
            System.out.printf("Wrote %d bytes of %s to file %s%n", total, body.contentType(), options.getFile());
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
            System.out.printf("%d Jobs in project %s%n", body.size(), options.getProject());
            for (JobItem jobItem : body) {
                System.out.println("* " + jobItem.toBasicString());
            }
        }
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
