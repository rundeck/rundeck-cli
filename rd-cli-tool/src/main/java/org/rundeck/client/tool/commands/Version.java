package org.rundeck.client.tool.commands;

import lombok.Setter;
import org.rundeck.client.RundeckClient;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.Main;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdOutput;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.tool.options.VerboseOption;
import picocli.CommandLine;

@CommandLine.Command(name = "version", description = "Print version information")
public class Version implements Runnable, RdCommandExtension, RdOutput {

    @Setter
    RdTool rdTool;
    @Setter
    private CommandOutput rdOutput;


    @CommandLine.Mixin()
    VerboseOption options;

    public void run() {
        if (options.isVerbose()) {
            rdOutput.output("VERSION: " + org.rundeck.client.Version.VERSION);
            rdOutput.output("GIT_COMMIT: " + org.rundeck.client.Version.GIT_COMMIT);
            rdOutput.output("GIT_BRANCH: " + org.rundeck.client.Version.GIT_BRANCH);
            rdOutput.output("GIT_DESCRIPTION: " + org.rundeck.client.Version.GIT_DESCRIPTION);
            rdOutput.output("BUILD_DATE: " + org.rundeck.client.Version.BUILD_DATE);
            rdOutput.output("API_VERS: " + RundeckClient.API_VERS);
            rdOutput.output("USER_AGENT: " + Main.USER_AGENT);
        } else {
            rdOutput.output(org.rundeck.client.Version.VERSION);
        }
    }
}
