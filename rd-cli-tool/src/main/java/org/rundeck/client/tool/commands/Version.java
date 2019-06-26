package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import org.rundeck.client.RundeckClient;
import org.rundeck.client.tool.Main;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;

@Command(synonyms = {"-V", "--version"}, description = "Print version information")
public class Version {

    @CommandLineInterface(application = "version/--version/-V")
    interface Options
            extends VerboseOption
    {

    }

    @Command(isSolo = true)
    public void version(CommandOutput output, Options options) {
        if (options.isVerbose()) {
            output.output("VERSION: " + org.rundeck.client.Version.VERSION);
            output.output("GIT_COMMIT: " + org.rundeck.client.Version.GIT_COMMIT);
            output.output("GIT_BRANCH: " + org.rundeck.client.Version.GIT_BRANCH);
            output.output("GIT_DESCRIPTION: " + org.rundeck.client.Version.GIT_DESCRIPTION);
            output.output("BUILD_DATE: " + org.rundeck.client.Version.BUILD_DATE);
            output.output("API_VERS: " + RundeckClient.API_VERS);
        } else {
            output.output(org.rundeck.client.Version.VERSION);
        }
    }
}
