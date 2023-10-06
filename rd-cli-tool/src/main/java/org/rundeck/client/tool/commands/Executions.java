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
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.api.model.executions.MetricsResponse;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.RdClientConfig;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.client.util.Util;
import picocli.CommandLine;

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
@CommandLine.Command(name = "executions", description = "List running executions, attach and follow their output, or kill them.")
public class Executions extends BaseCommand {
    @Getter
    @Setter
    static class KillOptions extends ExecutionIdOption{

        @CommandLine.Option(names = {"-f", "--force"}, description = "Force Incomplete")
        private boolean forceIncomplete;
    }

    @CommandLine.Command(description = "Attempt to kill an execution by ID.")
    public int kill(@CommandLine.Mixin KillOptions options) throws IOException, InputError {
        if (null == options.getId()) {
            throw new InputError("-e is required");
        }
        AbortResult abortResult = apiCall(api -> api.abortExecution(options.getId(), options.isForceIncomplete()));
        AbortResult.Reason abort = abortResult.abort;
        Execution execution = abortResult.execution;
        boolean failed = null != abort && "failed".equals(abort.status);

        getRdOutput().output(String.format("Kill [%s] result: %s", options.getId(), abort != null ? abort.status : null));

        if (null != execution) {
            getRdOutput().output(String.format("Execution [%s] status: %s", options.getId(), execution.getStatus()));
        }

        if (failed) {
            getRdOutput().warning(String.format("Kill request failed: %s", abort.reason));
        }
        return !failed ? 0 : 1;
    }


    @CommandLine.Command(description = "Delete an execution by ID.")
    public void delete(@CommandLine.Mixin ExecutionIdOption options) throws IOException, InputError {
        apiCall(api -> api.deleteExecution(options.getId()));
        getRdOutput().info(String.format("Delete [%s] succeeded.", options.getId()));
    }


    @CommandLine.Command(description = "Follow the output of an execution. Restart from the beginning, or begin tailing as it " +
            "runs.")
    public int follow(@CommandLine.Mixin ExecutionsFollowOptions options) throws IOException, InputError {

        int max = 500;

        ExecOutput output = startFollowOutput(
                getRdTool(),
                max,
                options.isRestart(),
                options.getId(),
                options.getTail(),
                true
        );


        return followOutput(
                getRdTool().getClient(),
                output,
                options.isProgress(),
                options.isQuiet(),
                options.getId(),
                max,
                getRdOutput(),
                options.isOutputFormat() ? Format.formatter(options.getOutputFormat(), ExecLog::toMap, "%", "") : null,
                waitUnlessInterrupt(2000)
        ) ? 0 : 1;
    }


    public static ExecOutput startFollowOutput(
            final RdTool rdTool,
            final long max,
            final boolean restart,
            final String id,
            final long tail,
            final boolean compacted
    ) throws IOException, InputError
    {

        ExecOutput out;
        if (restart) {
            out = rdTool.apiCallDowngradable( api -> api.getOutput(id, 0L, 0L, max, compacted));
        } else {
            out = rdTool.apiCallDowngradable( api -> api.getOutput(id, tail));
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
     * @param formatter formatter
     * @param waitFunc function for waiting, return false to halt
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


    @CommandLine.Command(description = "Get info about a single execution by ID.")
    public void info(@CommandLine.Mixin ExecutionIdOption options, @CommandLine.Mixin ExecutionOutputFormatOption outputFormatOption) throws IOException, InputError {

        Execution execution = apiCall(api -> api.getExecution(options.getId()));

        outputExecutionList(outputFormatOption, getRdOutput(), getRdTool().getAppConfig(), Stream.of(execution));
    }


    /*
    Executions state command
     */


    @CommandLine.Command(description = "Get detail about the node and step state of an execution by ID.")
    public void state(@CommandLine.Mixin ExecutionIdOption options) throws IOException, InputError {
        ExecutionStateResponse response = apiCall(api -> api.getExecutionState(options.getId()));
        getRdOutput().info(response.execInfoString(getRdTool().getAppConfig()));
        getRdOutput().output(response.nodeStatusString());
    }


    /* END state command */


    @CommandLine.Command(description = "List all running executions for a project.")
    public void list(@CommandLine.Mixin ExecutionOutputFormatOption outputFormatOption,
                     @CommandLine.Mixin PagingResultOptions paging,
                     @CommandLine.Mixin ProjectNameOptions projectNameOptions) throws IOException, InputError {
        int offset = paging.isOffset() ? paging.getOffset() : 0;
        int max = paging.isMax() ? paging.getMax() : 20;

        String project = getRdTool().projectOrEnv(projectNameOptions);
        ExecutionList executionList = apiCall(api -> api.runningExecutions(project, offset, max));

        if (!outputFormatOption.isOutputFormat()) {
            getRdOutput().info(String.format("Running executions: %d items%n", executionList.getPaging().getCount()));
        }

        outputExecutionList(outputFormatOption, getRdOutput(), getRdTool().getAppConfig(), executionList.getExecutions().stream());
    }


    @Getter
    @Setter
    static class BaseQuery extends QueryOptions {

        @CommandLine.Option(names = {"--noninteractive"},
                description = "Don't use interactive prompts to load more pages if there are more paged results (query command only)")
        private boolean nonInteractive;

        @CommandLine.Option(names = {"--autopage"},
                description = "Automatically load more results in non-interactive mode if there are more paged "
                        + "results. (query command only)")
        private boolean autoLoadPages;

    }

    @Getter
    @Setter
    static class QueryCmd extends BaseQuery implements HasJobIdList {
        @CommandLine.Option(
                names = {"--jobids", "-i"},
                arity = "1..*",
                description = "Job ID list to include"
        )
        private List<String> jobIdList;

    }

    @CommandLine.Command(description = "Query previous executions for a project.")
    public ExecutionList query(
            @CommandLine.Mixin QueryCmd options,
            @CommandLine.Mixin PagingResultOptions paging,
            @CommandLine.Mixin ExecutionOutputFormatOption outputFormatOption
    ) throws IOException, InputError {
        return query(false, options, options, paging, outputFormatOption);
    }


    public ExecutionList query(boolean disableInteractive, HasJobIdList jobIdList, BaseQuery options, PagingResultOptions paging, ExecutionOutputFormatOption outputFormatOption)
            throws IOException, InputError {
        CommandOutput out = getRdOutput();
        int offset = paging.isOffset() ? paging.getOffset() : 0;
        int max = paging.isMax() ? paging.getMax() : 20;

        Map<String, String> query = createQueryParams(options, max, offset);

        boolean interactive = !disableInteractive && !options.isNonInteractive();
        if (getRdTool().getAppConfig().getString("RD_FORMAT", null) != null) {
            interactive = false;
        }
        boolean autopage = interactive || options.isAutoLoadPages();

        String project = getRdTool().projectOrEnv(options);

        ExecutionList result = null;
        boolean verboseInfo = !outputFormatOption.isOutputFormat() && !autopage || interactive;
        List<Stream<Execution>> allResults = new ArrayList<>();
        while (offset >= 0) {
            query.put("offset", Integer.toString(offset));
            ExecutionList executionList = apiCall(api -> api
                    .listExecutions(
                            project,
                            query,
                            jobIdList.getJobIdList(),
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
                outputExecutionList(outputFormatOption, out, getRdTool().getAppConfig(), executionList.getExecutions().stream());
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
            outputExecutionList(outputFormatOption, out, getRdTool().getAppConfig(), allResults.stream().flatMap(a -> a));
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
            final OutputFormat options,
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
    @Getter @Setter
    static class DeleteAllExecCmd {
        @CommandLine.Option(names = {"--confirm", "-y"}, description = "Force confirmation of delete request.")
        private boolean confirm;

        @CommandLine.Option(names = {"-i", "--id"}, description = "Job ID")
        private String id;
    }

    @CommandLine.Command(description = "Delete all executions for a job.")
    public int deleteall(@CommandLine.Mixin DeleteAllExecCmd options) throws IOException, InputError {

        if (!options.isConfirm()) {
            //request confirmation
            if (!isInteractiveAvailable()) {
                getRdOutput().error("No user interaction available. Use --confirm to confirm purge without user interaction");
                getRdOutput().warning("Not deleting executions");
                return 2;
            }

            String s = readInteractive("Really delete all executions for job %s? (y/N) ", options.getId());

            if (!"y".equals(s)) {
                getRdOutput().warning("Not deleting executions.");
                return 2;
            }
        }

        BulkExecutionDeleteResponse result = apiCall(api -> api.deleteAllJobExecutions(options.getId()));
        if (!result.isAllsuccessful()) {
            getRdOutput().error(String.format("Failed to delete %d executions:", result.getFailedCount()));
            getRdOutput().error(result.getFailures()
                    .stream()
                    .map(BulkExecutionDeleteResponse.DeleteFailure::toString)
                    .collect(Collectors.toList()));
        } else {
            getRdOutput().info(String.format("Deleted %d executions.", result.getSuccessCount()));
        }
        return result.isAllsuccessful() ? 0 : 1;
    }

    static interface Interactive {
        boolean isEnabled();

        String readInteractive(String fmt, Object... args);
    }

    static class ConsoleInteractive implements Interactive {
        @Override
        public boolean isEnabled() {
            return System.console() != null;
        }

        @Override
        public String readInteractive(String fmt, Object... args) {
            return System.console().readLine(fmt, args);
        }
    }

    Interactive interactive = new ConsoleInteractive();

    private String readInteractive(String fmt, Object... args) {

        return interactive.readInteractive(fmt, args);
    }

    private boolean isInteractiveAvailable() {
        return interactive.isEnabled();
    }


    // End Delete all executions.

    interface HasJobIdList {
        List<String> getJobIdList();

        default boolean isJobIdList() {
            return getJobIdList() != null && getJobIdList().size() > 0;
        }
    }

    @Getter
    @Setter
    static class BulkDeleteCmd extends BaseQuery implements HasJobIdList {
        @CommandLine.Option(names = {"--confirm", "-y"}, description = "Force confirmation of delete request.")
        private boolean confirm;

        @CommandLine.Option(names = {"-i", "--idlist"}, description = "Comma separated list of Execution IDs")
        private String idlist;

        public boolean isIdlist() {
            return idlist != null;
        }

        @CommandLine.Option(
                names = {"--jobids"},
                arity = "1..*",
                description = "Job ID list to include"
        )
        private List<String> jobIdList;


        @CommandLine.Option(names = {"-R", "--require"},
                description = "Treat 0 query results as failure, otherwise succeed if no executions were returned")
        private boolean require;
    }

    @CommandLine.Command(description = "Find and delete executions in a project. Use the query options to find and delete " +
            "executions, or specify executions with the `idlist` option.")
    public int deletebulk(@CommandLine.Mixin BulkDeleteCmd options,
                              @CommandLine.Mixin PagingResultOptions paging,
                              @CommandLine.Mixin ExecutionOutputFormatOption outputFormatOption) throws IOException, InputError {

        List<String> execIds;
        if (options.isIdlist()) {
            execIds = Arrays.asList(options.getIdlist().split("\\s*,\\s*"));
        } else {
            ExecutionList executionList = query(true, options, options, paging, outputFormatOption);

            execIds = executionList.getExecutions()
                    .stream()
                    .map(Execution::getId)
                    .collect(Collectors.toList());
            if (execIds.size() < 1) {
                if (!options.isRequire()) {
                    getRdOutput().info("No executions found to delete");
                } else {
                    getRdOutput().warning("No executions found to delete");
                }
                return options.isRequire() ? 2 : 0;
            }
        }

        if (!options.isConfirm()) {
            //request confirmation
            String s = System.console().readLine("Really delete %d executions? (y/N) ", execIds.size());

            if (!"y".equals(s)) {
                getRdOutput().warning("Not deleting executions.");
                return 2;
            }
        }
        final List<String> finalExecIds = execIds;
        BulkExecutionDeleteResponse result = apiCall(api -> api.deleteExecutions(new BulkExecutionDelete
                                                                                         (finalExecIds)));
        if (!result.isAllsuccessful()) {
            getRdOutput().error(String.format("Failed to delete %d executions:", result.getFailedCount()));
            getRdOutput().error(result.getFailures()
                    .stream()
                    .map(BulkExecutionDeleteResponse.DeleteFailure::toString)
                    .collect(Collectors.toList()));
        }else{
            getRdOutput().info(String.format("Deleted %d executions.", result.getSuccessCount()));
        }
        return result.isAllsuccessful()?0:1;
    }

    public static boolean maybeFollow(
            final RdTool rdTool,
            final FollowOptions options,
            final OutputFormat formatOptions,
            final String id,
            CommandOutput output
    ) throws IOException, InputError
    {
        if (!options.isFollow()) {
            return true;
        }
        ExecOutput execOutputCall = startFollowOutput(
                rdTool,
                500,
                true,
                id,
                0,
                true
        );
        return followOutput(
                rdTool.getClient(),
                execOutputCall,
                options.isProgress(),
                options.isQuiet(),
                id,
                500,
                output,
                formatOptions.isOutputFormat() ? Format.formatter(formatOptions.getOutputFormat(), ExecLog::toMap, "%", "") : null,
                waitUnlessInterrupt(2000)
        );
    }

    /**
     * @param millis wait time
     *
     * @return wait function which returns false if interrupted true otherwise
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

    @Getter
    @Setter
    static class MetricsCmd extends QueryOptions implements OutputFormat {

        @CommandLine.Option(names = {"--jobids", "-i"},
                arity = "1..*",
                description = "Job ID list to include")
        private List<String> jobIdList;

        public boolean isJobIdList() {
            return jobIdList != null && jobIdList.size() > 0;
        }

        @CommandLine.Option(names = {"-%", "--outformat"},
                description =
                        "Output format specifier for execution metrics data. You can use \"%key\" where key is one "
                                + "of: total,failed-with-retry,failed,succeeded,duration-avg,duration-min,duration-max. E.g. "
                                + "\"%total %failed %succeeded\"")
        private String outputFormat;

        public boolean isOutputFormat() {
            return outputFormat != null;
        }


        @CommandLine.Option(names = {"--verbose", "-v"}, description = "Show verbose output")
        private boolean verbose;
    }


    @CommandLine.Command(description = "Obtain metrics over the result set of an execution query.")
    public void metrics(@CommandLine.Mixin MetricsCmd options) throws IOException, InputError {
        getRdTool().requireApiVersion("metrics", 29);

        Map<String, String> query = createQueryParams(options, null, null);

        MetricsResponse result;

        // Case project wire.
        if (options.isProject()) {
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
                getRdOutput().info("No results.");
                return;
            }
            getRdOutput().info(String.format("Showing stats for %d matching executions.", result.getTotal()));
            getRdOutput().output(result);
            return;
        }
        getRdOutput().output(Format.format(options.getOutputFormat(), result, "%", ""));
    }


}
