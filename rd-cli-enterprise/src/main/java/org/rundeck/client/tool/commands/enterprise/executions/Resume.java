package org.rundeck.client.tool.commands.enterprise.executions;

import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.ProjectInput;
import org.rundeck.client.tool.commands.enterprise.BaseExtension;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseResponse;
import org.rundeck.client.tool.commands.enterprise.api.model.ResumeResponse;
import org.rundeck.client.tool.commands.enterprise.api.model.ResumeStatus;
import org.rundeck.client.tool.extension.RdTool;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "resume", description = "Job Resume Enterprise Feature")
public class Resume extends BaseExtension {


    @Getter
    @Setter
    static class Options implements ProjectInput {
        @CommandLine.Option(names = {"-p", "--project"}, description = "Project")
        String project;
        @CommandLine.Option(names = {"-i", "--id"}, description = "Execution ID", required = true)
        String executionId;
        @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose")
        boolean verbose;
    }

    @CommandLine.Command(description = "Get Resume status for an execution")
    public boolean status(@CommandLine.Mixin Options options) throws IOException, InputError {
        String proj = getRdTool().projectOrEnv(options);
        RdTool.apiVersionCheck("execution resume status", 41, getClient().getApiVersion());
        ResumeStatus response = getClient().apiCall(api -> api.jobResumeStatus(proj, options.getExecutionId()));
        if (!options.verbose) {
            getOutput().info(String.format("Execution %s: resumable: %s", options.executionId, response.isResumeable()));
            getOutput().info(response.getMessage());
        } else {
            getOutput().info(response);
        }
        return response.isResumeable();
    }

    @CommandLine.Command(description = "Resume an execution")
    public boolean resume(@CommandLine.Mixin Options options) throws IOException, InputError {
        String proj = getRdTool().projectOrEnv(options);
        RdTool.apiVersionCheck("execution resume", 41, getClient().getApiVersion());
        ResumeResponse response = getClient().apiCall(api -> api.resumeExecution(proj, options.getExecutionId()));
        if (!options.verbose) {
            if (response.isSuccessful()) {
                getOutput().info(String.format("Resumed: %s", response.isSuccessful()));
                getOutput().info(String.format("New Execution ID %s: ", response.getExecutionId()));
                getOutput().info(response.getMessage());
            } else {
                getOutput().warning(response.getMessage());
            }
        } else {
            getOutput().info(response);
        }
        return response.isSuccessful();
    }
}
