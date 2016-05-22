package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.ExecutionList;
import org.rundeck.client.tool.options.RunBaseOptions;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.IOException;

/**
 * Created by greg on 5/20/16.
 */
public class Run {
    public static void main(final String[] args) throws IOException {
        Client<RundeckApi> client = App.prepareMain();
        boolean success = run(args, client);
        if (!success) {
            System.exit(2);
        }
    }

    private static boolean run(final String[] args, final Client<RundeckApi> client) throws IOException {
        RunBaseOptions options = CliFactory.parseArguments(RunBaseOptions.class, args);

        //todo: find job id by name
        if(options.isJob()){
            throw new UnsupportedOperationException("-j unsupported");
        }
        Call<Execution> executionListCall = client.getService().runJob(
                options.getId(),
                Adhoc.joinString(options.getCommandString()),
                options.getLoglevel(),
                options.getFilter()
        );
        Execution execution = client.checkError(executionListCall);
        System.out.printf("Execution started: %s%n", execution.toBasicString());

        return Executions.maybeFollow(client, options, execution.getId());
    }
}
