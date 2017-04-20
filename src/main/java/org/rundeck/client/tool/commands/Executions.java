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
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.AppConfig;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Format;
import retrofit2.Call;

import java.io.IOException;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * executions subcommands
 */
@Command(description = "List running executions, attach and follow their output, or kill them.")
public class Executions extends AppCommand {

    public Executions(final RdApp client) {
        super(client);
    }

    @CommandLineInterface(application = "kill") interface Kill extends ExecutionIdOption {

    }

    @Command(description = "Attempt to kill an execution by ID.")
    public boolean kill(Kill options, CommandOutput out) throws IOException, InputError {
        if (null == options.getId()) {
            throw new InputError("-e is required");
        }
        AbortResult abortResult = apiCall(api -> api.abortExecution(options.getId()));
        AbortResult.Reason abort = abortResult.abort;
        Execution execution = abortResult.execution;
        boolean failed = null != abort && "failed".equals(abort.status);

        out.output(String.format("Kill [%s] result: %s", options.getId(), abort != null ? abort.status : null));

        if (null != execution) {
            out.output(String.format("Execution [%s] status: %s", options.getId(), execution.getStatus()));
        }

        if (failed) {
            out.warning(String.format("Kill request failed: %s", abort.reason));
        }
        return !failed;
    }

    @CommandLineInterface(application = "delete") interface Delete extends ExecutionIdOption {

    }

    @Command(description = "Delete an execution by ID.")
    public void delete(Delete options, CommandOutput out) throws IOException, InputError {
        apiCall(api -> api.deleteExecution(options.getId()));
        out.info(String.format("Delete [%s] succeeded.", options.getId()));
    }

    @CommandLineInterface(application = "follow") interface Follow extends ExecutionsFollowOptions {

    }


    @Command(description = "Follow the output of an execution. Restart from the beginning, or begin tailing as it " +
                           "runs.")
    public boolean follow(Follow options, CommandOutput out) throws IOException, InputError {

        int max = 500;

        Call<ExecOutput> output = startFollowOutput(
                getClient(),
                max,
                options.isRestart(),
                options.getId(),
                options.getTail()
        );


        return followOutput(
                getClient(),
                output,
                options.isProgress(),
                options.isQuiet(),
                options.getId(),
                max,
                out,
                options.isOutputFormat() ? Format.formatter(options.getOutputFormat(), ExecLog::toMap, "%", "") : null
        );
    }


    public static Call<ExecOutput> startFollowOutput(
            final Client<RundeckApi> client,
            final long max,
            final boolean restart,
            final String id,
            final long tail
    )
    {

        Call<ExecOutput> out;
        if (restart) {
            out = client.getService().getOutput(id, 0L, 0L, max);
        } else {
            out = client.getService().getOutput(id, tail);
        }
        return out;
    }

    /**
     * Follow output, wait 2s between refreshing data from server, halts when interrupted
     *
     * @param progress show progress
     * @param quiet quell log output
     * @param id id
     * @param max max lines
     * @param out output
     *
     * @param formatter
     * @return true if successful
     *
     */
    public static boolean followOutput(
            final Client<RundeckApi> client,
            final Call<ExecOutput> output,
            final boolean progress,
            final boolean quiet,
            final String id,
            long max,
            CommandOutput out,
            final Function<ExecLog, String> formatter
    ) throws IOException
    {
        return followOutput(client, output, id, max, entries -> {
            if (progress && !entries.isEmpty()) {
                out.output(".");
            } else if (!quiet) {
                for (ExecLog entry : entries) {
                    String outval = formatter != null ? formatter.apply(entry) : entry.log;
                    if ("WARN".equals(entry.level)) {
                        out.warning(outval);
                    } else if ("ERROR".equals(entry.level)) {
                        out.error(outval);
                    } else {
                        out.output(outval);
                    }
                }
            }
        }, () -> {
            try {
                Thread.sleep(2000);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        });
    }

    /**
     * Follow output until execution completes and output is fully read, or interrupted
     * @param id  execution id
     * @param max max lines to retrieve with each request
     * @param receiver receive log events
     * @param waitFunc function for waiting, return false to halt
     *
     * @return true if execution is successful
     *
     */
    public static boolean followOutput(
            final Client<RundeckApi> client,
            final Call<ExecOutput> output,
            final String id,
            long max,
            Consumer<List<ExecLog>> receiver,
            BooleanSupplier waitFunc
    ) throws IOException
    {
        boolean done = false;
        String status = null;
        Call<ExecOutput> callOutput = output;
        while (!done) {
            ExecOutput execOutput = client.checkError(callOutput);
            receiver.accept(execOutput.entries);
            status = execOutput.execState;
            done = execOutput.execCompleted && execOutput.completed;
            if (!done) {
                if (!waitFunc.getAsBoolean()){
                    break;
                }
                callOutput = client.getService().getOutput(id, execOutput.offset, execOutput.lastModified, max);
            }
        }
        return "succeeded".equals(status);
    }

    @CommandLineInterface(application = "info") interface Info extends ExecutionIdOption, ExecutionResultOptions {

    }

    @Command(description = "List all running executions for a project.")
    public void info(Info options, CommandOutput out) throws IOException, InputError {

        Execution execution = apiCall(api -> api.getExecution(options.getId()));

        outputExecutionList(options, out, Collections.singletonList(execution), getAppConfig());
    }

    @CommandLineInterface(application = "list") interface ListCmd
            extends ExecutionListOptions, ProjectNameOptions, ExecutionResultOptions
    {

    }

    @Command(description = "List all running executions for a project.")
    public void list(ListCmd options, CommandOutput out) throws IOException, InputError {
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;

        String project = projectOrEnv(options);
        ExecutionList executionList = apiCall(api -> api.runningExecutions(project, offset, max));

        if (!options.isOutputFormat()) {
            out.info(String.format("Running executions: %d items%n", executionList.getPaging().getCount()));
        }

        outputExecutionList(options, out, executionList.getExecutions(), getAppConfig());
    }


    public interface ExecutionResultOptions extends ExecutionOutputFormatOption, VerboseOption {

    }

    @CommandLineInterface(application = "query") interface QueryCmd
            extends ExecutionListOptions, ProjectNameOptions, ExecutionResultOptions
    {
        @Option(shortName = "d",
                longName = "recent",
                description = "Get executions newer than specified time. e.g. \"3m\" (3 months). \n" +
                              "Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)")
        String getRecentFilter();

        boolean isRecentFilter();

        @Option(shortName = "O", longName = "older",
                description = "Get executions older than specified time. e.g. \"3m\" (3 months). \n" +
                              "Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)")
        String getOlderFilter();

        boolean isOlderFilter();

        @Option(shortName = "s", longName = "status",
                description = "Status filter, one of: running,succeeded,failed,aborted")
        String getStatusFilter();

        boolean isStatusFilter();

        @Option(shortName = "u", longName = "user",
                description = "User filter")
        String getUserFilter();

        boolean isUserFilter();

        @Option(shortName = "A", longName = "adhoconly",
                description = "Adhoc executions only")
        boolean isAdhoc();

        @Option(shortName = "J", longName = "jobonly",
                description = "Job executions only")
        boolean isJob();

        @Option(shortName = "i", longName = "jobids",
                description = "Job ID list to include")
        List<String> getJobIdList();

        boolean isJobIdList();

        @Option(shortName = "j", longName = "jobs",
                description = "List of Full job group and name to include.")
        List<String> getJobList();

        boolean isJobList();

        @Option(shortName = "x", longName = "xjobids",
                description = "Job ID list to exclude")
        List<String> getExcludeJobIdList();

        boolean isExcludeJobIdList();

        @Option(shortName = "X", longName = "xjobs",
                description = "List of Full job group and name to exclude.")
        List<String> getExcludeJobList();

        boolean isExcludeJobList();


        @Option(shortName = "g", longName = "group",
                description = "Group or partial group path to include, \"-\" means top-level jobs only")
        String getGroupPath();

        boolean isGroupPath();

        @Option(longName = "xgroup",
                description = "Group or partial group path to exclude, \"-\" means top-level jobs only")
        String getExcludeGroupPath();

        boolean isExcludeGroupPath();

        @Option(shortName = "G", longName = "groupexact",
                description = "Exact group path to include, \"-\" means top-level jobs only")
        String getGroupPathExact();

        boolean isGroupPathExact();

        @Option(longName = "xgroupexact",
                description = "Exact group path to exclude, \"-\" means top-level jobs only")
        String getExcludeGroupPathExact();

        boolean isExcludeGroupPathExact();

        @Option(shortName = "n", longName = "name",
                description = "Job Name Filter, include any name that matches this value")
        String getJobFilter();

        boolean isJobFilter();

        @Option(longName = "xname",
                description = "Exclude Job Name Filter, exclude any name that matches this value")
        String getExcludeJobFilter();

        boolean isExcludeJobFilter();

        @Option(shortName = "N", longName = "nameexact",
                description = "Exact Job Name Filter, include any name that is equal to this value")
        String getJobExactFilter();

        boolean isJobExactFilter();

        @Option(longName = "xnameexact",
                description = "Exclude Exact Job Name Filter, exclude any name that is equal to this value")
        String getExcludeJobExactFilter();

        boolean isExcludeJobExactFilter();



    }

    @Command(description = "Query previous executions for a project.")
    public ExecutionList query(QueryCmd options, CommandOutput out) throws IOException, InputError {
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;
        Map<String, String> query = new HashMap<>();
        query.put("offset", Integer.toString(offset));
        query.put("max", Integer.toString(max));
        if (options.isRecentFilter()) {
            query.put("recentFilter", options.getRecentFilter());
        }
        if (options.isOlderFilter()) {
            query.put("olderFilter", options.getOlderFilter());
        }
        if (options.isStatusFilter()) {
            query.put("statusFilter", options.getStatusFilter());
        }
        if (options.isUserFilter()) {
            query.put("userFilter", options.getUserFilter());
        }
        if (options.isAdhoc()) {
            query.put("adhoc", "true");
        } else if (options.isJob()) {
            query.put("adhoc", "false");
        }
        if (options.isGroupPath()) {
            query.put("groupPath", options.getGroupPath());
        }
        if (options.isExcludeGroupPath()) {
            query.put("excludeGroupPath", options.getExcludeGroupPath());
        }
        if (options.isExcludeGroupPathExact()) {
            query.put("excludeGroupPathExact", options.getExcludeGroupPathExact());
        }
        if (options.isJobFilter()) {
            query.put("jobFilter", options.getJobFilter());
        }
        if (options.isExcludeJobFilter()) {
            query.put("excludeJobFilter", options.getExcludeJobFilter());
        }

        if (options.isJobExactFilter()) {
            query.put("jobExactFilter", options.getJobExactFilter());
        }
        if (options.isExcludeJobExactFilter()) {
            query.put("excludeJobExactFilter", options.getExcludeJobExactFilter());
        }


        String project = projectOrEnv(options);
        ExecutionList executionList = apiCall(api -> api
                .listExecutions(
                        project,
                        query,
                        options.getJobIdList(),
                        options.getExcludeJobIdList(),
                        options.getJobList(),
                        options.getExcludeJobList()
                ));

        Paging page = executionList.getPaging();
        if (!options.isOutputFormat()) {
            out.info(page);
        }
        outputExecutionList(options, out, executionList.getExecutions(), getAppConfig());
        if (!options.isOutputFormat()) {
            out.info(page.moreResults("-o"));
        }
        return executionList;
    }

    public static void outputExecutionList(
            final ExecutionResultOptions options,
            final CommandOutput out,
            final List<Execution> executionList,
            final AppConfig config
    )
    {
        if (options.isVerbose()) {

            out.output(executionList.stream().map(e -> e.getInfoMap(config)).collect(Collectors.toList()));
            return;
        }
        final Function<Execution, ?> outformat;
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), e -> e.getInfoMap(config), "%", "");
        } else {
            outformat = e -> e.toExtendedString(config);
        }
        executionList.forEach(e -> out.output(outformat.apply(e)));
    }

    @CommandLineInterface(application = "deletebulk") interface BulkDeleteCmd extends QueryCmd {
        @Option(longName = "confirm", shortName = "y", description = "Force confirmation of delete request.")
        boolean isConfirm();

        @Option(shortName = "i", longName = "idlist", description = "Comma separated list of Execution IDs")
        String getIdlist();

        boolean isIdlist();
    }

    @Command(description = "Find and delete executions in a project. Use the query options to find and delete " +
                           "executions, or specify executions with the `idlist` option.")
    public boolean deletebulk(BulkDeleteCmd options, CommandOutput out) throws IOException, InputError {

        List<String> execIds;
        if (options.isIdlist()) {
            execIds = Arrays.asList(options.getIdlist().split("\\s*,\\s*"));
        } else {
            ExecutionList executionList = query(options, out);

            execIds = executionList.getExecutions()
                                   .stream()
                                   .map(Execution::getId)
                                   .collect(Collectors.toList());
        }
        if (null == execIds || execIds.size() < 1) {
            out.warning("No executions found to delete");
            return false;
        }

        if (!options.isConfirm()) {
            //request confirmation
            String s = System.console().readLine("Really delete %d executions? (y/N) ", execIds.size());

            if (!"y".equals(s)) {
                out.warning("Not deleting executions.");
                return false;
            }
        }
        final List<String> finalExecIds = execIds;
        BulkExecutionDeleteResponse result = apiCall(api -> api.deleteExecutions(new BulkExecutionDelete
                                                                                         (finalExecIds)));
        if (!result.isAllsuccessful()) {
            out.error(String.format("Failed to delete %d executions:", result.getFailedCount()));
            out.error(result.getFailures()
                            .stream()
                            .map(BulkExecutionDeleteResponse.DeleteFailure::toString)
                            .collect(Collectors.toList()));
        }else{
            out.info(String.format("Deleted %d executions.", result.getSuccessCount()));
        }
        return result.isAllsuccessful();
    }

    public static boolean maybeFollow(
            final Client<RundeckApi> client,
            final FollowOptions options,
            final String id,
            CommandOutput output
    ) throws IOException
    {
        if (!options.isFollow()) {
            return true;
        }
        Call<ExecOutput> execOutputCall = startFollowOutput(
                client,
                500,
                true,
                id,
                0
        );
        return followOutput(
                client,
                execOutputCall,
                options.isProgress(),
                options.isQuiet(),
                id,
                500,
                output,
                options.isOutputFormat() ? Format.formatter(options.getOutputFormat(), ExecLog::toMap, "%", "") : null
        );
    }
}
