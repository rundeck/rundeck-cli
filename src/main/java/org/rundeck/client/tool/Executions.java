package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.AbortResult;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.ExecutionList;
import org.rundeck.client.tool.options.ExecutionsOptions;

import java.io.IOException;

/**
 * Created by greg on 5/20/16.
 */
public class Executions {

    public static void main(String[] args) throws IOException {
        String baseUrl = App.requireEnv("RUNDECK_URL", "Please specify the Rundeck URL");
        String token = App.requireEnv("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
        RundeckApi client = Rundeck.client(baseUrl, token, System.getenv("DEBUG") != null);
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

    private static boolean kill(final String[] args, final RundeckApi client) throws IOException {
        ExecutionsOptions options = CliFactory.parseArguments(ExecutionsOptions.class, args);
        if (null == options.getId()) {
            throw new IllegalArgumentException("-e is required");
        }
        AbortResult abortResult = App.checkError(client.abortExecution(options.getId()));
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

    private static void follow(final String[] args, final RundeckApi client) {

    }

    private static void list(final String[] args, final RundeckApi client) throws IOException {

        ExecutionsOptions options = CliFactory.parseArguments(ExecutionsOptions.class, args);
        if (!options.isProject()) {
            throw new IllegalArgumentException("-p is required");
        }
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;

        ExecutionList executionList = App.checkError(client.listExecutions(options.getProject(), offset, max));
        System.out.printf("Running executions: %d item%n", executionList.getPaging().getCount());
        for (Execution execution : executionList.getExecutions()) {
            System.out.println(execution.toBasicString());
        }
    }
}
