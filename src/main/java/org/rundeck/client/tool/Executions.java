package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.options.ExecutionsFollowOptions;
import org.rundeck.client.tool.options.ExecutionsOptions;
import org.rundeck.client.tool.options.FollowOptions;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
public class Executions {

    public static void main(String[] args) throws IOException {
        Client<RundeckApi> client = App.prepareMain();
        String[] actions = new String[]{"list", "follow", "kill"};
        boolean success = true;
        if ("follow".equals(args[0])) {
            follow(App.tail(args), client);
        } else if ("kill".equals(args[0])) {
            success = kill(App.tail(args), client);
        } else {
            list("list".equals(args[0]) ? App.tail(args) : args, client);
        }
        if (!success) {
            System.exit(2);
        }
    }

    private static boolean kill(final String[] args, final Client<RundeckApi> client) throws IOException {
        ExecutionsOptions options = CliFactory.parseArguments(ExecutionsOptions.class, args);
        if (null == options.getId()) {
            throw new IllegalArgumentException("-e is required");
        }
        AbortResult abortResult = client.checkError(client.getService().abortExecution(options.getId()));
        AbortResult.Reason abort = abortResult.abort;
        Execution execution = abortResult.execution;
        boolean failed = null != abort && "failed".equals(abort.status);

        System.out.println(String.format("Kill [%s] result: %s", options.getId(), abort.status));

        if (null != execution) {
            System.out.println(String.format("Execution [%s] status: %s", options.getId(), execution.getStatus()));
        }

        if (failed) {
            System.out.println(String.format("Kill request failed: %s", abort.reason));
        }
        return !failed;
    }

    private static boolean follow(final String[] args, final Client<RundeckApi> client) throws IOException {
        ExecutionsFollowOptions options = CliFactory.parseArguments(ExecutionsFollowOptions.class, args);

        int max = 500;
//        System.out.printf("options : %s %s%n", options, options.isRestart());

        Call<ExecOutput> output = startFollowOutput(client, max, options.isRestart(), options.getId(), options.getTail());


        return followOutput(client, output, options.isProgress(), options.isQuiet(), options.getId(), max);
    }

    public static Call<ExecOutput> startFollowOutput(
            final Client<RundeckApi> client,
            final long max, final boolean restart, final String id, final long tail
    )
    {
        Call<ExecOutput> output;
        if (restart) {
            output = client.getService().getOutput(id, 0L, 0L, max);
        } else {
            output = client.getService().getOutput(id, tail);
        }
        return output;
    }

    public static boolean followOutput(
            final Client<RundeckApi> client,
            final Call<ExecOutput> output,
            final boolean progress,
            final boolean quiet,
            final String id,
            long max
    ) throws IOException
    {
        boolean done = false;
        String status = null;
        Call<ExecOutput> callOutput = output;
        while (!done) {
            ExecOutput execOutput = client.checkError(callOutput);
            printLogOutput(execOutput.entries, progress, quiet);
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

    private static void printLogOutput(final List<ExecLog> entries, final boolean progress, final boolean quiet) {
        if (!quiet && !progress) {
            for (ExecLog entry : entries) {
                System.out.println(entry.log);
            }
        } else if (progress && entries.size()>0) {
            System.out.print(".");
        }
    }

    private static void list(final String[] args, final Client<RundeckApi> client) throws IOException {

        ExecutionsOptions options = CliFactory.parseArguments(ExecutionsOptions.class, args);
        if (!options.isProject()) {
            throw new IllegalArgumentException("-p is required");
        }
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;

        ExecutionList executionList = client.checkError(client.getService().listExecutions(options.getProject(), offset, max));
        System.out.printf("Running executions: %d item%n", executionList.getPaging().getCount());
        for (Execution execution : executionList.getExecutions()) {
            System.out.println(execution.toBasicString());
        }
    }

    public static boolean maybeFollow(
            final Client<RundeckApi> client,
            final FollowOptions options,
            final String id
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
                500
        );
    }
}
