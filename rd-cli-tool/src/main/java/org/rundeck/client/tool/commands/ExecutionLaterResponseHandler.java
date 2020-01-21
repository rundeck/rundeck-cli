package org.rundeck.client.tool.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rundeck.client.api.model.ExecutionModeLaterResponse;
import org.rundeck.client.api.model.repository.ArtifactActionMessage;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.toolbelt.CommandOutput;

import java.io.IOException;

public class ExecutionLaterResponseHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ExecutionModeLaterResponse handle(final ServiceClient.WithErrorResponse<ExecutionModeLaterResponse>
                                      response, final CommandOutput output) throws IOException {
        ExecutionModeLaterResponse msg = response.getResponse().body();

        if(response.isError400()) {
            return msg;
        } else if(response.getResponse().code() == 403) {
            output.error("Server returned a 403. Either you don't have access to the API or the execution later plugin feature is not enabled.");
        } else if(response.getResponse().code() == 404) {
            output.error("Server returned a 404. Either you don't have access to the API or the execution later plugin feature is not enabled.");
        } else {
            return msg;
        }

        ExecutionModeLaterResponse responseData = new ExecutionModeLaterResponse();
        responseData.setSaved(false);
        responseData.setMsg("Error processing API");
        return responseData;
    }
}
