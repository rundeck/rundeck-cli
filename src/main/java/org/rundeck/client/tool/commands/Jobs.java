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
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.HasSubCommands;
import com.simplifyops.toolbelt.InputError;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.RdApp;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * jobs subcommands
 */
@Command(description = "List and manage Jobs.")
public class Jobs extends AppCommand implements HasSubCommands {

    public static final String UUID_REMOVE = "remove";
    public static final String UUID_PRESERVE = "preserve";

    public Jobs(final RdApp client) {
        super(client);
    }


    @Override
    public List<Object> getSubCommands() {
        return Collections.singletonList(
                new Files(this)
        );
    }

    @CommandLineInterface(application = "purge") interface Purge extends JobPurgeOptions, ListOpts {
        @Option(longName = "confirm", shortName = "y", description = "Force confirmation of delete request.")
        boolean isConfirm();
    }

    @Command(description = "Delete jobs matching the query parameters. Optionally save the definitions to a file " +
                           "before deleting from the server. " +
                           "--idlist/-i, or --job/-j or --group/-g or --jobxact/-J or --groupxact/-G Options are " +
                           "required.")
    public boolean purge(Purge options, CommandOutput output) throws IOException, InputError {

        //if id,idlist specified, use directly
        //otherwise query for the list and assemble the ids

        List<String> ids = new ArrayList<>();
        if (options.isIdlist()) {
            ids = Arrays.asList(options.getIdlist().split("\\s*,\\s*"));
        } else {
            if (!options.isJob() && !options.isGroup() && !options.isGroupExact() && !options.isJobExact()) {
                throw new InputError("must specify -i, or -j/-g/-J/-G to specify jobs to delete.");
            }
            String project = projectOrEnv(options);
            List<JobItem> body = apiCall(api -> api.listJobs(
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

        if (options.isFile()) {
            list(options, output);
        }
        if (!options.isConfirm()) {
            //request confirmation
            if (null == System.console()) {
                output.error("No user interaction available. Use --confirm to confirm purge without user interaction");
                output.warning(String.format("Not deleting %d jobs", ids.size()));
                return false;
            }
            String s = System.console().readLine("Really delete %d Jobs? (y/N) ", ids.size());

            if (!"y".equals(s)) {
                output.warning(String.format("Not deleting %d jobs", ids.size()));
                return false;
            }
        }

        final List<String> finalIds = ids;
        DeleteJobsResult deletedJobs = apiCall(api -> api.deleteJobs(finalIds));

        if (deletedJobs.isAllsuccessful()) {
            output.info(String.format("%d Jobs were deleted%n", deletedJobs.getRequestCount()));
            return true;
        }
        output.error(String.format("Failed to delete %d Jobs%n", deletedJobs.getFailed().size()));
        output.output(deletedJobs.getFailed().stream().map(DeleteJob::toBasicString).collect(Collectors.toList()));
        return false;
    }

    @CommandLineInterface(application = "load") interface Load extends JobLoadOptions, VerboseOption {
    }

    @Command(description = "Load Job definitions from a file in XML or YAML format.")
    public boolean load(Load options, CommandOutput output) throws IOException, InputError {
        if (!options.isFile()) {
            throw new InputError("-f is required");
        }
        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new InputError(String.format("File is not readable or does not exist: %s", input));
        }

        RequestBody requestBody = RequestBody.create(
                "xml".equals(options.getFormat()) ? Client.MEDIA_TYPE_XML : Client.MEDIA_TYPE_YAML,
                input
        );

        String project = projectOrEnv(options);
        ImportResult importResult = apiCall(api -> api.loadJobs(
                project,
                requestBody,
                options.getFormat(),
                options.getDuplicate(),
                options.isRemoveUuids() ? UUID_REMOVE : UUID_PRESERVE
        ));

        List<JobLoadItem> failed = importResult.getFailed();

        printLoadResult(importResult.getSucceeded(), "Succeeded", output, options.isVerbose());
        printLoadResult(importResult.getSkipped(), "Skipped", output, options.isVerbose());
        printLoadResult(failed, "Failed", output, options.isVerbose());

        return failed == null || failed.isEmpty();
    }

    private static void printLoadResult(
            final List<JobLoadItem> list,
            final String title,
            CommandOutput output, final boolean isVerbose
    )
    {
        if (null != list && !list.isEmpty()) {
            output.info(String.format("%d Jobs %s:%n", list.size(), title));
            if (isVerbose) {
                output.output(list);
            } else {
                output.output(list.stream().map(JobLoadItem::toBasicString).collect(Collectors.toList()));
            }
        }
    }

    interface JobResultOptions extends JobOutputFormatOption, VerboseOption {

    }

    @CommandLineInterface(application = "list") interface ListOpts extends JobListOptions, JobResultOptions {
    }

    @Command(description = "List jobs found in a project, or download Job definitions (-f).")
    public void list(ListOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        if (options.isFile()) {
            //write response to file instead of parsing it
            Call<ResponseBody> responseCall;
            if (options.isIdlist()) {
                responseCall = getClient().getService().exportJobs(
                        project,
                        options.getIdlist(),
                        options.getFormat()
                );
            } else {
                responseCall = getClient().getService().exportJobs(
                        project,
                        options.getJob(),
                        options.getGroup(),
                        options.getJobExact(),
                        options.getGroupExact(),
                        options.getFormat()
                );
            }
            ResponseBody body = getClient().checkError(responseCall);
            if ((!"yaml".equals(options.getFormat()) ||
                 !ServiceClient.hasAnyMediaType(body, Client.MEDIA_TYPE_YAML, Client.MEDIA_TYPE_TEXT_YAML)) &&
                !ServiceClient.hasAnyMediaType(body, Client.MEDIA_TYPE_XML, Client.MEDIA_TYPE_TEXT_XML)) {

                throw new IllegalStateException("Unexpected response format: " + body.contentType());
            }
            InputStream inputStream = body.byteStream();
            if ("-".equals(options.getFile().getName())) {
                Util.copyStream(inputStream, System.out);
            } else {
                try (FileOutputStream out = new FileOutputStream(options.getFile())) {
                    long total = Util.copyStream(inputStream, out);
                    if (!options.isOutputFormat()) {
                        output.info(String.format(
                                "Wrote %d bytes of %s to file %s%n",
                                total,
                                body.contentType(),
                                options.getFile()
                        ));
                    }
                }
            }
        } else {
            Call<List<JobItem>> listCall;
            if (options.isIdlist()) {
                listCall = getClient().getService().listJobs(project, options.getIdlist());
            } else {
                listCall = getClient().getService().listJobs(
                        project,
                        options.getJob(),
                        options.getGroup(),
                        options.getJobExact(),
                        options.getGroupExact()
                );
            }
            List<JobItem> body = getClient().checkError(listCall);
            if (!options.isOutputFormat()) {
                output.info(String.format("%d Jobs in project %s%n", body.size(), project));
            }
            outputJobList(options, output, body);
        }
    }

    private void outputJobList(final JobResultOptions options, final CommandOutput output, final List<JobItem> body) {
        final Function<JobItem, ?> outformat;
        if (options.isVerbose()) {
            output.output(body.stream().map(JobItem::toMap).collect(Collectors.toList()));
            return;
        }
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), JobItem::toMap, "%", "");
        } else {
            outformat = JobItem::toBasicString;
        }

        output.output(body.stream().map(outformat).collect(Collectors.toList()));
    }

    @CommandLineInterface(application = "info") interface InfoOpts extends JobResultOptions {

        @Option(shortName = "i", longName = "id", description = "Job ID")
        String getId();
    }

    interface ToggleOpts extends JobIdentOptions {

        @Option(shortName = "j",
                longName = "job",
                description = "Job job (group and name). Select a Job specified by Job name and group. eg: " +
                              "'group/name'. Requires specifying the Project name.")
        String getJob();

        boolean isJob();

        @Option(shortName = "i", longName = "id", description = "Select the Job with this IDENTIFIER")
        String getId();

        boolean isId();
    }

    @Command(description = "Get info about a Job by ID (API v18)")
    public void info(InfoOpts options, CommandOutput output) throws IOException, InputError {
        ScheduledJobItem body = apiCall(api -> api.getJobInfo(options.getId()));
        outputJobList(options, output, Collections.singletonList(body));
    }

    @CommandLineInterface(application = "enable") interface EnableOpts extends ToggleOpts {
    }

    @Command(description = "Enable execution for a job")
    public boolean enable(EnableOpts options, CommandOutput output) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobExecutionEnable, options, output, "Enabled Job %s");
    }

    @CommandLineInterface(application = "disable") interface DisableOpts extends ToggleOpts {
    }

    @Command(description = "Disable execution for a job")
    public boolean disable(DisableOpts options, CommandOutput output) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobExecutionDisable, options, output, "Disabled Job %s");
    }

    @CommandLineInterface(application = "enableSchedule") interface EnableSchedOpts extends ToggleOpts {
    }

    @Command(description = "Enable schedule for a job")
    public boolean reschedule(EnableSchedOpts options, CommandOutput output) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobScheduleEnable, options, output, "Enabled Schedule for Job %s");
    }

    @CommandLineInterface(application = "disableSchedule") interface DisableSchedOpts extends ToggleOpts {
    }

    @Command(description = "Disable schedule for a job")
    public boolean unschedule(DisableSchedOpts options, CommandOutput output) throws IOException, InputError {
        return simpleJobApiCall(RundeckApi::jobExecutionDisable, options, output, "Disabled Schedule for Job %s");
    }

    private boolean simpleJobApiCall(
            BiFunction<RundeckApi, String, Call<Simple>> func,
            final ToggleOpts options,
            final CommandOutput output,
            final String success
    )
            throws InputError, IOException
    {
        String jobId = Run.getJobIdFromOpts(
                options,
                output,
                getClient(),
                () -> projectOrEnv(options)
        );
        if (null == jobId) {
            return false;
        }
        Simple simple = apiCall(api -> func.apply(api, jobId));
        if (simple.isSuccess()) {
            output.info(String.format(success, jobId));
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
        if ("".equals(group.trim())) {
            group = null;
        }
        return new String[]{group, name};

    }
}
