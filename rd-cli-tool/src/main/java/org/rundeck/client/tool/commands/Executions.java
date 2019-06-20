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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.api.model.executions.MetricsResponse;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.RdClientConfig;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.client.util.Util;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;

import java.io.IOException;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * executions subcommands
 */
@Command(description = "List running executions, attach and follow their output, or kill them.")
public class Executions extends AppCommand {

    private static final ObjectMapper JSON = new ObjectMapper();


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

        ExecOutput output = startFollowOutput(
                getRdApp(),
                max,
                options.isRestart(),
                options.getId(),
                options.getTail(),
                true
        );


        return followOutput(
                getClient(),
                output,
                options.isProgress(),
                options.isQuiet(),
                options.getId(),
                max,
                out,
                options.isOutputFormat() ? Format.formatter(options.getOutputFormat(), ExecLog::toMap, "%", "") : null,
                waitUnlessInterrupt(2000)
        );
    }


    public static ExecOutput startFollowOutput(
            final RdApp rdApp,
            final long max,
            final boolean restart,
            final String id,
            final long tail,
            final boolean compacted
    ) throws IOException, InputError
    {

        ExecOutput out;
        if (restart) {
            out = apiCallDowngradable(rdApp, api -> api.getOutput(id, 0L, 0L, max, compacted));
        } else {
            out = apiCallDowngradable(rdApp, api -> api.getOutput(id, tail));
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
     * @param waitFunc
     * @return true if successful
     *
     */
    public static boolean followOutput(
            final ServiceClient<RundeckApi> serviceClient,
            final ExecOutput output,
            final boolean progress,
            final boolean quiet,
            final String id,
            long max,
            CommandOutput out,
            final Function<ExecLog, String> formatter,
            final BooleanSupplier waitFunc
    ) throws IOException
    {
        return followOutput(serviceClient, output, id, max, true, entries -> {
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
        }, waitFunc);
    }

    /**
     * Follow output until execution completes and output is fully read, or interrupted
     * @param id  execution id
     * @param max max lines to retrieve with each request
     * @param compacted if true, request compacted data
     * @param receiver receive log events
     * @param waitFunc function for waiting, return false to halt
     *
     * @return true if execution is successful
     *
     */
    public static boolean followOutput(
            final ServiceClient<RundeckApi> serviceClient,
            final ExecOutput output,
            final String id,
            long max,
            final boolean compacted,
            Consumer<List<ExecLog>> receiver,
            BooleanSupplier waitFunc
    ) throws IOException
    {
        boolean done = false;
        String status = null;
        ExecOutput execOutput = output;
        while (!done) {
            receiver.accept(execOutput.decompactEntries());
            status = execOutput.execState;
            done = execOutput.execCompleted && execOutput.completed;
            if (!done) {
                if (!waitFunc.getAsBoolean()){
                    break;
                }
                final ExecOutput passOutput = execOutput;
                execOutput = serviceClient.apiCall(api -> api.getOutput(
                        id,
                        passOutput.offset,
                        passOutput.lastModified,
                        max,
                        compacted
                ));
            }
        }
        return "succeeded".equals(status);
    }

    @CommandLineInterface(application = "info") interface Info extends ExecutionIdOption, ExecutionResultOptions {

    }

    @Command(description = "Get info about a single execution by ID.")
    public void info(Info options, CommandOutput out) throws IOException, InputError {

        Execution execution = apiCall(api -> api.getExecution(options.getId()));

        outputExecutionList(options, out, getAppConfig(), Collections.singletonList(execution).stream());
    }


    /*
    Executions state command
     */
    @CommandLineInterface(application = "state") interface State extends ExecutionIdOption {
    }

    @Command(description = "Get detail about the node and step state of an execution by ID.")
    public void state(State options, CommandOutput out) throws IOException, InputError {
        ExecutionStateResponse response = apiCall(api -> api.getExecutionState(options.getId()));
        out.info(response.execInfoString(getAppConfig()));
        out.output(response.nodeStatusString());
    }


    /* END state command */


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

        outputExecutionList(options, out, getAppConfig(), executionList.getExecutions().stream());
    }


    @CommandLineInterface(application = "query")
    interface QueryCmd extends QueryOptions, ExecutionResultOptions, ExecutionListOptions  {

        @Option(longName = "noninteractive",
            description = "Don't use interactive prompts to load more pages if there are more paged results (query command only)")
        boolean isNonInteractive();

        @Option(longName = "autopage",
            description = "Automatically load more results in non-interactive mode if there are more paged "
                + "results. (query command only)")
        boolean isAutoLoadPages();

    }

    @Command(description = "Query previous executions for a project.")
    public ExecutionList query(QueryCmd options, CommandOutput out) throws IOException, InputError {
        return query(false, options, out);
    }


    public ExecutionList query(boolean disableInteractive, QueryCmd options, CommandOutput out)
            throws IOException, InputError {
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;

        Map<String, String> query = createQueryParams(options, max, offset);

        boolean interactive = !disableInteractive && !options.isNonInteractive();
        if (getAppConfig().getString("RD_FORMAT", null) != null) {
            interactive = false;
        }
        boolean autopage = interactive || options.isAutoLoadPages();

        String project = projectOrEnv(options);

        ExecutionList result = null;
        boolean verboseInfo = !options.isOutputFormat() && !autopage || interactive;
        List<Stream<Execution>> allResults = new ArrayList<>();
        while (offset >= 0) {
            query.put("offset", Integer.toString(offset));
            ExecutionList executionList = apiCall(api -> api
                    .listExecutions(
                            project,
                            query,
                            options.getJobIdList(),
                            options.getExcludeJobIdList(),
                            options.getJobList(),
                            options.getExcludeJobList()
                    ));
            result = executionList;
            Paging page = executionList.getPaging();
            if (verboseInfo) {
                out.info(page);
            }
            allResults.add(executionList.getExecutions().stream());

            if (interactive) {
                outputExecutionList(options, out, getAppConfig(), executionList.getExecutions().stream());
            }
            if (verboseInfo && !autopage) {
                out.info(page.moreResults("-o",
                                          page.hasMoreResults() && !disableInteractive
                                          ? ", or --autopage for all"
                                          : null
                ));
            }
            if (!autopage) {
                break;
            }
            if (!page.hasMoreResults()) {
                break;
            }
            //next page by default
            offset = page.nextPageOffset();
            if (interactive) {
                //prompt for next paging
                int maxpage = page.maxPagenum();
                int nextpage = page.pagenum() + 1;

                int i = Util.readPrompt(
                        String.format("Enter page to load 1-%d [default: %d]: ", maxpage, nextpage),
                        (input) -> {
                            if ("".equals(input) || "n".equalsIgnoreCase(input) || "next".equalsIgnoreCase(input)) {
                                return Optional.of(0);
                            }
                            if ("exit".equalsIgnoreCase(input)
                                || "quit".equalsIgnoreCase(input)
                                || "q".equalsIgnoreCase(input)) {
                                return Optional.of(-1);
                            }
                            try {
                                int value = Integer.parseInt(input);
                                if (value > maxpage) {
                                    out.warning(String.format("Maximum page number is: %d", maxpage));
                                    return Optional.empty();
                                }
                                if (value < 1) {
                                    out.warning("Minimum page number is: 1");
                                    return Optional.empty();
                                }
                                return Optional.of(value);
                            } catch (NumberFormatException e) {
                                out.error(String.format("Not a valid number: %s", input));
                                return Optional.empty();
                            }

                        },
                        -1
                );

                if (i > 0) {
                    offset = page.getMax() * (i - 1);
                } else if (i == 0) {
                    offset = page.nextPageOffset();
                } else {
                    offset = -1;
                }

            }

        }
        if (!interactive) {
            outputExecutionList(options, out, getAppConfig(), allResults.stream().flatMap(a -> a));
        }
        return result;
    }

    private Map<String, String> createQueryParams(
            final QueryOptions options,
            final Integer max,
            final Integer offset
    ) {
        final Map<String, String> query = new HashMap<>();

        if(max != null) {
            query.put("max", Integer.toString(max));
        }
        if(offset != null) {
            query.put("offset", Integer.toString(offset));
        }
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
        return query;
    }

    public static void outputExecutionList(
            final ExecutionResultOptions options,
            final CommandOutput out,
            final RdClientConfig config,
            final Stream<Execution> executions
    )
    {
        if (options.isVerbose()) {

            out.output(executions.map(e -> e.getInfoMap(config)).collect(Collectors.toList()));
            return;
        }
        final Function<Execution, ?> outformat;
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), e -> e.getInfoMap(config), "%", "");
        } else {
            outformat = e -> e.toExtendedString(config);
        }
        executions.forEach(e -> out.output(outformat.apply(e)));
    }


    // Delete All executions for job command.
    @CommandLineInterface(application = "deleteall") interface DeleteAllExecCmd {
        @Option(longName = "confirm", shortName = "y", description = "Force confirmation of delete request.")
        boolean isConfirm();

        @Option(shortName = "i", longName = "id", description = "Job ID")
        String getId();
    }

    @Command(description = "Delete all executions for a job.")
    public boolean deleteall(DeleteAllExecCmd options, CommandOutput out) throws IOException, InputError {

        if (!options.isConfirm()) {
            //request confirmation
            String s = System.console().readLine("Really delete all executions for job %s? (y/N) ", options.getId());

            if (!"y".equals(s)) {
                out.warning("Not deleting executions.");
                return false;
            }
        }

        BulkExecutionDeleteResponse result = apiCall(api -> api.deleteAllJobExecutions(options.getId()));
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




    // End Delete all executions.


    @CommandLineInterface(application = "deletebulk") interface BulkDeleteCmd extends QueryCmd {
        @Option(longName = "confirm", shortName = "y", description = "Force confirmation of delete request.")
        boolean isConfirm();

        @Option(shortName = "i", longName = "idlist", description = "Comma separated list of Execution IDs")
        String getIdlist();

        boolean isIdlist();

        @Option(shortName = "R",
                longName = "require",
                description = "Treat 0 query results as failure, otherwise succeed if no executions were returned")
        boolean require();
    }

    @Command(description = "Find and delete executions in a project. Use the query options to find and delete " +
                           "executions, or specify executions with the `idlist` option.")
    public boolean deletebulk(BulkDeleteCmd options, CommandOutput out) throws IOException, InputError {

        List<String> execIds;
        if (options.isIdlist()) {
            execIds = Arrays.asList(options.getIdlist().split("\\s*,\\s*"));
        } else {
            ExecutionList executionList = query(true, options, out);

            execIds = executionList.getExecutions()
                                   .stream()
                                   .map(Execution::getId)
                                   .collect(Collectors.toList());
            if (null == execIds || execIds.size() < 1) {
                if (!options.require()) {
                    out.info("No executions found to delete");
                } else {
                    out.warning("No executions found to delete");
                }
                return !options.require();
            }
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
            final RdApp rdApp,
            final FollowOptions options,
            final String id,
            CommandOutput output
    ) throws IOException, InputError
    {
        if (!options.isFollow()) {
            return true;
        }
        ExecOutput execOutputCall = startFollowOutput(
                rdApp,
                500,
                true,
                id,
                0,
                true
        );
        return followOutput(
                rdApp.getClient(),
                execOutputCall,
                options.isProgress(),
                options.isQuiet(),
                id,
                500,
                output,
                options.isOutputFormat() ? Format.formatter(options.getOutputFormat(), ExecLog::toMap, "%", "") : null,
                waitUnlessInterrupt(2000)
        );
    }

    /**
     * @param millis wait time
     *
     * @return wait function which returns false if interrupted, true otherwise
     */
    private static BooleanSupplier waitUnlessInterrupt(final int millis) {
        return () -> {
            try {
                Thread.sleep(millis);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        };
    }

    @CommandLineInterface(application = "metrics")
    interface MetricsCmd
            extends QueryOptions
    {

        @Option(
                longName = "xml",
                description = "Get the result in raw xml. Note: cannot be combined with RD_FORMAT env variable.")
        boolean isRawXML();


        @Option(shortName = "%",
                longName = "outformat",
                description =
                        "Output format specifier for execution metrics data. You can use \"%key\" where key is one "
                        + "of: total,failed-with-retry,failed,succeeded,duration-avg,duration-min,duration-max. E.g. "
                        + "\"%total %failed %succeeded\"")
        String getOutputFormat();

        boolean isOutputFormat();
    }


    @Command(description = "Obtain metrics over the result set of an execution query.")
    public void metrics(MetricsCmd options, CommandOutput out) throws IOException, InputError {
        requireApiVersion("metrics", 29);

        // Check parameters.
        if (!"xml".equalsIgnoreCase(getAppConfig().getString("RD_FORMAT", "xml")) && options.isRawXML()) {
            throw new InputError("You cannot use RD_FORMAT env var with --xml");
        }

        Map<String, String> query = createQueryParams(options, null, null);

        MetricsResponse result;

        // Case project wire.
        if (options.isProject()) {

            // Raw XML
            if ("XML".equalsIgnoreCase(getAppConfig().getString("RD_FORMAT", null)) || options.isRawXML()) {
                ResponseBody response = apiCall(api -> api.executionMetricsXML(
                        options.getProject(),
                        query,
                        options.getJobIdList(),
                        options.getExcludeJobIdList(),
                        options.getJobList(),
                        options.getExcludeJobList()
                ));
                out.output(response.string());
                return;
            }

            // Get response.
            result = apiCall(api -> api.executionMetrics(
                    options.getProject(),
                    query,
                    options.getJobIdList(),
                    options.getExcludeJobIdList(),
                    options.getJobList(),
                    options.getExcludeJobList()
            ));

        }

        // Case system-wide
        else {

            // Raw XML
            if ("XML".equalsIgnoreCase(getAppConfig().getString("RD_FORMAT", null)) || options.isRawXML()) {
                ResponseBody response = apiCall(api -> api.executionMetricsXML(
                        query,
                        options.getJobIdList(),
                        options.getExcludeJobIdList(),
                        options.getJobList(),
                        options.getExcludeJobList()
                ));
                out.output(response.string());
                return;
            }

            // Get raw Json.
            result = apiCall(api -> api.executionMetrics(
                    query,
                    options.getJobIdList(),
                    options.getExcludeJobIdList(),
                    options.getJobList(),
                    options.getExcludeJobList()
            ));

        }

        if (!options.isOutputFormat()) {
            if (result.getTotal() == null || result.getTotal() < 1) {
                out.info("No results.");
                return;
            }
            out.info(String.format("Showing stats for %d matching executions.", result.getTotal()));
            out.output(result);
            return;
        }
        out.output(Format.format(options.getOutputFormat(), result, "%", ""));
    }


}
