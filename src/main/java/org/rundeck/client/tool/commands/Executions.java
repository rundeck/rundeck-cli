package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Client;
import org.rundeck.util.toolbelt.Command;
import org.rundeck.util.toolbelt.CommandOutput;
import retrofit2.Call;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by greg on 5/20/16.
 */

@Command(description = "List running executions, attach and follow their output, or kill them.")
public class Executions extends ApiCommand {

    public Executions(final Client<RundeckApi> client) {
        super(client);
    }

    @CommandLineInterface(application = "kill") interface Kill extends ExecutionIdOption {

    }

    @Command(description = "Attempt to kill an execution by ID.")
    public boolean kill(Kill options, CommandOutput out) throws IOException {
        if (null == options.getId()) {
            throw new IllegalArgumentException("-e is required");
        }
        AbortResult abortResult = client.checkError(client.getService().abortExecution(options.getId()));
        AbortResult.Reason abort = abortResult.abort;
        Execution execution = abortResult.execution;
        boolean failed = null != abort && "failed".equals(abort.status);

        out.output(String.format("Kill [%s] result: %s", options.getId(), abort.status));

        if (null != execution) {
            out.output(String.format("Execution [%s] status: %s", options.getId(), execution.getStatus()));
        }

        if (failed) {
            out.output(String.format("Kill request failed: %s", abort.reason));
        }
        return !failed;
    }

    @CommandLineInterface(application = "delete") interface Delete extends ExecutionIdOption {

    }

    @Command(description = "Delete an execution by ID.")
    public void delete(Delete options, CommandOutput out) throws IOException {
        client.checkError(client.getService().deleteExecution(options.getId()));
        out.output(String.format("Delete [%s] succeeded.", options.getId()));
    }

    @CommandLineInterface(application = "follow") interface Follow extends ExecutionsFollowOptions {

    }


    @Command(description = "Follow the output of an execution. Restart from the beginning, or begin tailing as it " +
                           "runs.")
    public boolean follow(Follow options, CommandOutput out) throws IOException {

        int max = 500;

        Call<ExecOutput> output = startFollowOutput(
                client,
                max,
                options.isRestart(),
                options.getId(),
                options.getTail()
        );


        return followOutput(client, output, options.isProgress(), options.isQuiet(), options.getId(), max, out);
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

    public static boolean followOutput(
            final Client<RundeckApi> client,
            final Call<ExecOutput> output,
            final boolean progress,
            final boolean quiet,
            final String id,
            long max,
            CommandOutput out
    ) throws IOException
    {
        boolean done = false;
        String status = null;
        Call<ExecOutput> callOutput = output;
        while (!done) {
            ExecOutput execOutput = client.checkError(callOutput);
            printLogOutput(execOutput.entries, progress, quiet, out);
            done = execOutput.completed;
            status = execOutput.execState;
            if (!done) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }

                callOutput = client.getService().getOutput(id, execOutput.offset, execOutput.lastModified, max);
            }
        }
        return "succeeded".equals(status);
    }

    private static void printLogOutput(
            final List<ExecLog> entries,
            final boolean progress,
            final boolean quiet,
            CommandOutput out
    )
    {

        if (!quiet && !progress) {
            for (ExecLog entry : entries) {
                out.output(entry.log);
            }
        } else if (progress && entries.size() > 0) {
            out.output(".");
        }
    }


    @CommandLineInterface(application = "list") interface ListCmd extends ExecutionListOptions, ProjectNameOptions {

    }

    @Command(isDefault = true, description = "List all running executions for a project.")
    public void list(ListCmd options, CommandOutput out) throws IOException {
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;

        ExecutionList executionList = client.checkError(client.getService()
                                                              .runningExecutions(options.getProject(), offset, max));
        out.output(String.format("Running executions: %d items%n", executionList.getPaging().getCount()));
        for (Execution execution : executionList.getExecutions()) {
            out.output(execution.toBasicString());
        }
    }

    @CommandLineInterface(application = "query") interface QueryCmd extends ExecutionListOptions, ProjectNameOptions {
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
    }

    @Command(isDefault = true, description = "Query previous executions for a project.")
    public void query(QueryCmd options, CommandOutput out) throws IOException {
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;

        ExecutionList executionList = client.checkError(client.getService()
                                                              .listExecutions(options.getProject(), offset, max,
                                                                              options.getOlderFilter(),
                                                                              options.getRecentFilter()
                                                              ));

        Paging page = executionList.getPaging();
        out.output(String.format(
                "Found executions: %d of %d%n",
                page.getCount(),
                page.getTotal()
        ));
        for (Execution execution : executionList.getExecutions()) {
            try {
                out.output(execution.toExtendedString());
            } catch (ParseException e) {
                out.output(execution.toBasicString());
            }
        }
        if (page.getTotal() >
            (page.getOffset() + page.getCount())) {

            int nextOffset = page.getOffset() + page.getMax();
            out.output(String.format("(more results available, append: -o %d)", nextOffset));
        } else {
            out.output(String.format("End of results."));
        }
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
                output
        );
    }
}
