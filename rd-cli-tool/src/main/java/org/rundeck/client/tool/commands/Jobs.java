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
import okhttp3.MediaType;
import org.rundeck.client.tool.extension.BaseCommand;
import picocli.CommandLine;
import org.rundeck.client.api.model.scheduler.ForecastJobItem;
import org.rundeck.client.api.model.scheduler.ScheduledJobItem;
import org.rundeck.client.tool.InputError;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.commands.jobs.Files;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.client.util.Util;
import retrofit2.Call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * jobs subcommands
 */
@CommandLine.Command(
        name = "jobs",
        description = "List and manage Jobs.",
        subcommands = {
                Files.class
        })
public class Jobs extends BaseCommand {

    public static final String UUID_REMOVE = "remove";
    public static final String UUID_PRESERVE = "preserve";


    @Getter @Setter
    public static class Purge {
        @CommandLine.Option(names = {"--confirm", "-y"}, description = "Force confirmation of delete request.")
        boolean confirm;

        @CommandLine.Option(names = {"--batch", "-b"}, description = "Batch size if there are many IDs")
        Integer batchSize;

        boolean isBatchSize() {
            return batchSize != null && batchSize > 0;
        }


        @CommandLine.Option(names = {"--max", "-m"}, description = "Maximum number of jobs to delete")
        Integer max;

        boolean isMax() {
            return max != null && max > 0;
        }

    }

    @CommandLine.Command(description = "Delete jobs matching the query parameters. Optionally save the definitions to a file " +
            "before deleting from the server. " +
            "--idlist/-i, or --job/-j or --group/-g or --jobxact/-J or --groupxact/-G Options are " +
            "required.")
    public int purge(@CommandLine.Mixin Purge options,
                         @CommandLine.Mixin JobOutputFormatOption jobOutputFormatOption,
                         @CommandLine.Mixin JobFileOptions jobFileOptions,
                         @CommandLine.Mixin JobListOptions jobListOptions) throws IOException, InputError {

        //if id,idlist specified, use directly
        //otherwise query for the list and assemble the ids

        List<String> ids = new ArrayList<>();
        if (jobListOptions.isIdlist()) {
            ids = jobListOptions.getIdlist();
        } else {
            if (!jobListOptions.isJob() && !jobListOptions.isGroup() && !jobListOptions.isGroupExact() && !jobListOptions.isJobExact()) {
                throw new InputError("must specify -i, or -j/-g/-J/-G to specify jobs to delete.");
            }
            String project = getRdTool().projectOrEnv(jobListOptions);
            List<JobItem> body = getRdTool().apiCall(api -> api.listJobs(
                    project,
                    jobListOptions.getJob(),
                    jobListOptions.getGroup(),
                    jobListOptions.getJobExact(),
                    jobListOptions.getGroupExact()
            ));
            for (JobItem jobItem : body) {
                ids.add(jobItem.getId());
            }
        }

        if (jobFileOptions.isFile()) {
            list(jobOutputFormatOption, jobFileOptions, jobListOptions);
        }
        int idsSize = ids.size();
        int idsToDelete = options.isMax() ? Math.min(idsSize, options.getMax()) : idsSize;
        if (!options.isConfirm()) {
            //request confirmation
            if (null == System.console()) {
                getRdOutput().error("No user interaction available. Use --confirm to confirm purge without user interaction");
                getRdOutput().warning(String.format("Not deleting %d jobs", idsToDelete));
                return 2;
            }
            String s = System.console().readLine("Really delete %d Jobs? (y/N) ", idsToDelete);

            if (!"y".equals(s)) {
                getRdOutput().warning(String.format("Not deleting %d jobs", idsToDelete));
                return 2;
            }
        }
        int batch = options.isBatchSize() ? Math.min(idsToDelete, options.getBatchSize()) : idsToDelete;
        int total = 0;
        for (int i = 0; i < idsToDelete; ) {
            int batchToUse = Math.min(batch, idsToDelete - total);
            final List<String> finalIds = new ArrayList<>(batchToUse);
            finalIds.addAll(ids.subList(i, i + batchToUse));
            DeleteJobsResult deletedJobs = getRdTool().apiCall(api -> api.deleteJobsBulk(new BulkJobDelete(finalIds)));
            if (!deletedJobs.isAllsuccessful()) {
                getRdOutput().error(String.format("Failed to delete %d Jobs%n", deletedJobs.getFailed().size()));
                getRdOutput().output(deletedJobs.getFailed().stream().map(DeleteJob::toBasicString).collect(Collectors.toList()));
                return 1;
            }
            total += finalIds.size();
            i += batchToUse;
        }

        getRdOutput().info(String.format("%d Jobs were deleted%n", total));
        return 0;
    }

    @CommandLine.Command(description = "Load Job definitions from a file in XML, YAML or JSON format.")
    public int load(
            @CommandLine.Mixin JobLoadOptions options,
            @CommandLine.Mixin JobFileOptions fileOptions,
            @CommandLine.Mixin ProjectNameOptions projectNameOptions,
            @CommandLine.Mixin VerboseOption verboseOption
    ) throws IOException, InputError {
        if (!fileOptions.isFile()) {
            throw new InputError("-f is required");
        }
        File input = fileOptions.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new InputError(String.format("File is not readable or does not exist: %s", input));
        }
        MediaType mediaType = Client.MEDIA_TYPE_XML;
        if (fileOptions.getFormat() == JobFileOptions.Format.yaml) {
            mediaType = Client.MEDIA_TYPE_YAML;
        } else if (fileOptions.getFormat() == JobFileOptions.Format.json) {
            mediaType = Client.MEDIA_TYPE_JSON;
        }
        RequestBody requestBody = RequestBody.create(
                input,
                mediaType
        );

        String project = getRdTool().projectOrEnv(projectNameOptions);
        ImportResult importResult = getRdTool().apiCall(api -> api.loadJobs(
                project,
                requestBody,
                fileOptions.getFormat().toString(),
                options.getDuplicate().toString(),
                options.isRemoveUuids() ? UUID_REMOVE : UUID_PRESERVE
        ));

        List<JobLoadItem> failed = importResult.getFailed();

        printLoadResult(importResult.getSucceeded(), "Succeeded", verboseOption.isVerbose());
        printLoadResult(importResult.getSkipped(), "Skipped", verboseOption.isVerbose());
        printLoadResult(failed, "Failed", verboseOption.isVerbose());

        return (failed == null || failed.isEmpty()) ? 0 : 1;
    }

    private void printLoadResult(
            final List<JobLoadItem> list,
            final String title,
            final boolean isVerbose
    ) {
        if (null != list && !list.isEmpty()) {
            getRdOutput().info(String.format("%d Jobs %s:%n", list.size(), title));
            if (isVerbose) {
                getRdOutput().output(list);
            } else {
                getRdOutput().output(list.stream().map(JobLoadItem::toBasicString).collect(Collectors.toList()));
            }
        }
    }




    @CommandLine.Command(description = "List jobs found in a project, or download Job definitions (-f).")
    public void list(
            @CommandLine.Mixin JobOutputFormatOption jobOutputFormatOption,
            @CommandLine.Mixin JobFileOptions jobFileOptions,
            @CommandLine.Mixin JobListOptions jobListOptions
    ) throws IOException, InputError {
        String project = getRdTool().projectOrEnv(jobListOptions);
        if (jobFileOptions.isFile()) {
            //write response to file instead of parsing it
            ResponseBody body1;
            if (jobListOptions.isIdlist()) {
                body1 = getRdTool().apiCall(api -> api.exportJobs(
                        project,
                        String.join(",", jobListOptions.getIdlist()),
                        jobFileOptions.getFormat().toString()
                ));
            } else {
                body1 = getRdTool().apiCall(api -> api.exportJobs(
                        project,
                        jobListOptions.getJob(),
                        jobListOptions.getGroup(),
                        jobListOptions.getJobExact(),
                        jobListOptions.getGroupExact(),
                        jobFileOptions.getFormat().toString()
                ));
            }
            try (ResponseBody body = body1) {
                if ((
                            jobFileOptions.getFormat() == JobFileOptions.Format.yaml
                            && !ServiceClient.hasAnyMediaType(
                                    body.contentType(),
                                    Client.MEDIA_TYPE_YAML,
                                    Client.MEDIA_TYPE_TEXT_YAML
                            )
                    ) || (
                            jobFileOptions.getFormat() == JobFileOptions.Format.json
                            && !ServiceClient.hasAnyMediaType(
                                    body.contentType(),
                                    Client.MEDIA_TYPE_JSON
                            )
                    ) || (
                            jobFileOptions.getFormat() == JobFileOptions.Format.xml
                            && !ServiceClient.hasAnyMediaType(
                                    body.contentType(),
                                    Client.MEDIA_TYPE_XML,
                                    Client.MEDIA_TYPE_TEXT_XML
                            )
                    )) {

                    throw new IllegalStateException("Unexpected response format: " + body.contentType());
                }
                InputStream inputStream = body.byteStream();
                if ("-".equals(jobFileOptions.getFile().getName())) {
                    Util.copyStream(inputStream, System.out);
                } else {
                    try (FileOutputStream out = new FileOutputStream(jobFileOptions.getFile())) {
                        long total = Util.copyStream(inputStream, out);
                        if (!jobOutputFormatOption.isOutputFormat()) {
                            getRdOutput().info(String.format(
                                    "Wrote %d bytes of %s to file %s%n",
                                    total,
                                    body.contentType(),
                                    jobFileOptions.getFile()
                            ));
                        }
                    }
                }
            }
        } else {
            List<JobItem> body;
            if (jobListOptions.isIdlist()) {
                body = getRdTool().apiCall(api -> api.listJobs(project, String.join(",", jobListOptions.getIdlist())));
            } else {
                body = getRdTool().apiCall(api -> api.listJobs(
                        project,
                        jobListOptions.getJob(),
                        jobListOptions.getGroup(),
                        jobListOptions.getJobExact(),
                        jobListOptions.getGroupExact()
                ));
            }
            if (!jobOutputFormatOption.isOutputFormat()) {
                getRdOutput().info(String.format("%d Jobs in project %s%n", body.size(), project));
            }
            outputJobList(jobOutputFormatOption, body);
        }
    }

    private void outputJobList(final JobOutputFormatOption options, final List<JobItem> body) {
        final Function<JobItem, ?> outformat;
        if (options.isVerbose()) {
            getRdOutput().output(body.stream().map(JobItem::toMap).collect(Collectors.toList()));
            return;
        }
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), JobItem::toMap, "%", "");
        } else {
            outformat = JobItem::toBasicString;
        }

        getRdOutput().output(body.stream().map(outformat).collect(Collectors.toList()));
    }


    @CommandLine.Command(description = "Get info about a Job by ID (API v18)")
    public void info(
            @CommandLine.Option(names = {"-i", "--id"}, description = "Job ID", required = true) String id,
            @CommandLine.Mixin JobOutputFormatOption outputFormatOption
    ) throws IOException, InputError {
        ScheduledJobItem body = getRdTool().apiCall(api -> api.getJobInfo(id));
        outputJobList(outputFormatOption, Collections.singletonList(body));
    }

    @CommandLine.Command(description = "Get Schedule Forecast for a Job by ID (API v31)")
    public void forecast(
            @CommandLine.Option(names = {"-i", "--id"}, description = "Job ID", required = true)
            String id,

            @CommandLine.Option(names = {"-t", "--time"}, description = "Time ahead using number+unit. e.g. 1h (1 hour).\n " +
                    "Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)")
            String time,

            @CommandLine.Option(names = {"-m", "--max"}, description = "Max number of results")
            Integer max
    ) throws IOException, InputError {
        getRdTool().requireApiVersion("jobs forecast", 31);
        ForecastJobItem body = getRdTool().apiCall(api -> api.getJobForecast(id, time, max));
        getRdOutput().output("Forecast:");
        if (body.getFutureScheduledExecutions() != null) {
            getRdOutput().output(body.getFutureScheduledExecutions());
        }
    }


    @CommandLine.Command(description = "Enable execution for a job")
    public int enable(@CommandLine.Mixin JobIdentOptions options) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobExecutionEnable, options, "Enabled Job %s") ? 0 : 1;
    }

    @CommandLine.Command(description = "Disable execution for a job")
    public int disable(@CommandLine.Mixin JobIdentOptions options) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobExecutionDisable, options, "Disabled Job %s") ? 0 : 1;
    }


    @CommandLine.Command(description = "Enable schedule for a job")
    public int reschedule(@CommandLine.Mixin JobIdentOptions options) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobScheduleEnable, options, "Enabled Schedule for Job %s") ? 0 : 1;
    }

    @CommandLine.Command(description = "Disable schedule for a job")
    public int unschedule(@CommandLine.Mixin JobIdentOptions options) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobScheduleDisable, options, "Disabled Schedule for Job %s") ? 0 : 1;
    }

    private boolean simpleJobApiCall(
            BiFunction<RundeckApi, String, Call<Simple>> func,
            final JobIdentOptions options,
            final String success
    )
            throws InputError, IOException {
        String jobId = Run.getJobIdFromOpts(
                options,
                getRdOutput(),
                getRdTool(),
                () -> getRdTool().projectOrEnv(options)
        );
        if (null == jobId) {
            return false;
        }
        Simple simple = getRdTool().apiCall(api -> func.apply(api, jobId));
        if (simple.isSuccess()) {
            getRdOutput().info(String.format(success, jobId));
        }
        return simple.isSuccess();
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
        int i = job.lastIndexOf('/');
        String group = job.substring(0, i);
        String name = job.substring(i + 1);
        if (group.trim().isEmpty()) {
            group = null;
        }
        return new String[]{group, name};

    }

    /* Bulk toggle execution */

    private List<String> getJobList(BulkJobActionOptions options) throws InputError, IOException {

        //if id,idlist specified, use directly
        //otherwise query for the list and assemble the ids

        List<String> ids = new ArrayList<>();
        if (options.isIdlist()) {
            ids = options.getIdlist();
        }
        else {
            if (!options.isJob() && !options.isGroup() && !options.isGroupExact() && !options.isJobExact()) {
                throw new InputError("must specify -i, or -j/-g/-J/-G to specify jobs to enable.");
            }
            String project = getRdTool().projectOrEnv(options);
            List<JobItem> body = getRdTool().apiCall(api -> api.listJobs(
                    project,
                    options.getJob(),
                    options.getGroup(),
                    options.getJobExact(),
                    options.getGroupExact()
            ));
            for (JobItem jobItem : body) {
                ids.add(jobItem.getId());
            }
        }

        return ids;
    }


    @CommandLine.Command(description = "Enable execution for a set of jobs. " +
            "--idlist/-i, or --job/-j or --group/-g or --jobxact/-J or --groupxact/-G Options are " +
            "required.")
    public int enablebulk(@CommandLine.Mixin BulkJobActionOptions options, @CommandLine.Mixin VerboseOption verboseOption) throws IOException, InputError {

        List<String> ids = getJobList(options);

        if (!options.isConfirm()) {
            //request confirmation
            if (null == System.console()) {
                getRdOutput().error("No user interaction available. Use --confirm to confirm request without user interaction");
                getRdOutput().warning(String.format("Not enabling %d jobs", ids.size()));
                return 2;
            }
            String s = System.console().readLine("Really enable %d Jobs? (y/N) ", ids.size());

            if (!"y".equals(s)) {
                getRdOutput().warning(String.format("Not enabling %d jobs", ids.size()));
                return 2;
            }
        }

        final List<String> finalIds = ids;

        BulkToggleJobExecutionResponse response = getRdTool().apiCall(api -> api.bulkEnableJobs(new IdList(finalIds)));

        if (response.isAllsuccessful()) {
            getRdOutput().info(String.format("%d Jobs were enabled%n", response.getRequestCount()));
            if (verboseOption.isVerbose()) {
                getRdOutput().output(response.getSucceeded().stream()
                        .map(BulkToggleJobExecutionResponse.Result::toString)
                        .collect(Collectors.toList()));
            }
            return 0;
        }
        getRdOutput().error(String.format("Failed to enable %d Jobs%n", response.getFailed().size()));
        getRdOutput().output(response.getFailed().stream()
                .map(BulkToggleJobExecutionResponse.Result::toString)
                .collect(Collectors.toList()));
        return 1;
    }


    @CommandLine.Command(description = "Disable execution for a set of jobs. " +
            "--idlist/-i, or --job/-j or --group/-g or --jobxact/-J or --groupxact/-G Options are " +
            "required.")
    public int disablebulk(@CommandLine.Mixin BulkJobActionOptions options, @CommandLine.Mixin VerboseOption verboseOption) throws IOException, InputError {

        List<String> ids = getJobList(options);

        if (!options.isConfirm()) {
            //request confirmation
            if (null == System.console()) {
                getRdOutput().error("No user interaction available. Use --confirm to confirm request without user interaction");
                getRdOutput().warning(String.format("Not disabling %d jobs", ids.size()));
                return 2;
            }
            String s = System.console().readLine("Really disable %d Jobs? (y/N) ", ids.size());

            if (!"y".equals(s)) {
                getRdOutput().warning(String.format("Not disabling %d jobs", ids.size()));
                return 2;
            }
        }

        final List<String> finalIds = ids;

        BulkToggleJobExecutionResponse response = getRdTool().apiCall(api -> api.bulkDisableJobs(new IdList(finalIds)));

        if (response.isAllsuccessful()) {
            getRdOutput().info(String.format("%d Jobs were disabled%n", response.getRequestCount()));
            if (verboseOption.isVerbose()) {
                getRdOutput().output(response.getSucceeded().stream()
                        .map(BulkToggleJobExecutionResponse.Result::toString)
                        .collect(Collectors.toList()));
            }
            return 0;
        }
        getRdOutput().error(String.format("Failed to disable %d Jobs%n", response.getFailed().size()));
        getRdOutput().output(response.getFailed().stream()
                .map(BulkToggleJobExecutionResponse.Result::toString)
                .collect(Collectors.toList()));
        return 1;
    }


    @CommandLine.Command(description = "Enable schedule for a set of jobs. " +
            "--idlist/-i, or --job/-j or --group/-g or --jobxact/-J or --groupxact/-G Options are " +
            "required.")
    public int reschedulebulk(@CommandLine.Mixin BulkJobActionOptions options, @CommandLine.Mixin VerboseOption verboseOption) throws IOException, InputError {

        List<String> ids = getJobList(options);

        if (!options.isConfirm()) {
            //request confirmation
            if (null == System.console()) {
                getRdOutput().error("No user interaction available. Use --confirm to confirm request without user interaction");
                getRdOutput().warning(String.format("Not rescheduling %d jobs", ids.size()));
                return 2;
            }
            String s = System.console().readLine("Really reschedule %d Jobs? (y/N) ", ids.size());

            if (!"y".equals(s)) {
                getRdOutput().warning(String.format("Not rescheduling %d jobs", ids.size()));
                return 2;
            }
        }

        final List<String> finalIds = ids;

        BulkToggleJobScheduleResponse response = getRdTool().apiCall(api -> api.bulkEnableJobSchedule(new IdList(finalIds)));

        if (response.isAllsuccessful()) {
            getRdOutput().info(String.format("%d Jobs were rescheduled%n", response.getRequestCount()));
            if (verboseOption.isVerbose()) {
                getRdOutput().output(response.getSucceeded().stream()
                        .map(BulkToggleJobScheduleResponse.Result::toString)
                        .collect(Collectors.toList()));
            }
            return 0;
        }
        getRdOutput().error(String.format("Failed to reschedule %d Jobs%n", response.getFailed().size()));
        getRdOutput().output(response.getFailed().stream()
                .map(BulkToggleJobScheduleResponse.Result::toString)
                .collect(Collectors.toList()));
        return 1;
    }


    @CommandLine.Command(description = "Disable schedule for a set of jobs. " +
            "--idlist/-i, or --job/-j or --group/-g or --jobxact/-J or --groupxact/-G Options are " +
            "required.")
    public int unschedulebulk(@CommandLine.Mixin BulkJobActionOptions options, @CommandLine.Mixin VerboseOption verboseOption) throws IOException, InputError {

        List<String> ids = getJobList(options);

        if (!options.isConfirm()) {
            //request confirmation
            if (null == System.console()) {
                getRdOutput().error("No user interaction available. Use --confirm to confirm request without user interaction");
                getRdOutput().warning(String.format("Not unscheduling %d jobs", ids.size()));
                return 2;
            }
            String s = System.console().readLine("Really unschedule %d Jobs? (y/N) ", ids.size());

            if (!"y".equals(s)) {
                getRdOutput().warning(String.format("Not unscheduling %d jobs", ids.size()));
                return 2;
            }
        }

        final List<String> finalIds = ids;

        BulkToggleJobScheduleResponse response = getRdTool().apiCall(api -> api.bulkDisableJobSchedule(new IdList(finalIds)));

        if (response.isAllsuccessful()) {
            getRdOutput().info(String.format("%d Jobs were unsheduled%n", response.getRequestCount()));
            if (verboseOption.isVerbose()) {
                getRdOutput().output(response.getSucceeded().stream()
                        .map(BulkToggleJobScheduleResponse.Result::toString)
                        .collect(Collectors.toList()));
            }
            return 0;
        }
        getRdOutput().error(String.format("Failed to disable %d Jobs%n", response.getFailed().size()));
        getRdOutput().output(response.getFailed().stream()
                .map(BulkToggleJobScheduleResponse.Result::toString)
                .collect(Collectors.toList()));
        return 1;
    }



}
