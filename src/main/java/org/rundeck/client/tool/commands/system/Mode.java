package org.rundeck.client.tool.commands.system;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ExecutionMode;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.api.model.SystemMode;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.options.QuietOption;
import org.rundeck.client.util.ServiceClient;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author greg
 * @since 8/14/17
 */

@Command(description = "Manage Execution Mode")
public class Mode extends AppCommand {
    public Mode(final RdApp rdApp) {
        super(rdApp);
    }

    @CommandLineInterface(application = "info") interface ModeInfo {

        @Option(shortName = "A",
                longName = "testactive",
                description = "Test whether the execution mode is active: fail if not")
        boolean isTestActive();

        @Option(shortName = "P",
                longName = "testpassive",
                description = "Test whether the execution mode is passive: fail if not")
        boolean isTestPassive();
    }

    @Command(description =
                     "Show execution mode\n" +
                     "When --testactive or --testpassive are used, the exit code will be 0 if the test is successful, 1 otherwise.")
    public boolean info(ModeInfo opts, CommandOutput output) throws IOException, InputError {
        if (opts.isTestPassive() && opts.isTestActive()) {
            throw new InputError("--testactive and --testpassive cannot be combined");
        }
        SystemInfo systemInfo = apiCall(RundeckApi::systemInfo);
        Object executionMode = systemInfo.system.getExecutions().get("executionMode");
        boolean modeIsActive = "active".equals(executionMode);
        boolean testpass = true;
        String message = "Execution Mode is currently:";

        if (opts.isTestActive() && !modeIsActive || opts.isTestPassive() && modeIsActive) {
            testpass = false;
            output.warning(message);
        } else {
            output.info(message);
        }
        output.output(executionMode);
        return testpass;
    }

    @CommandLineInterface(application = "active") interface ModeActive extends QuietOption {

    }

    @Command(description = "Set execution mode Active")
    public boolean active(ModeActive opts, CommandOutput output) throws IOException, InputError {
        return changeMode(opts, output, ExecutionMode.active, RundeckApi::executionModeEnable);
    }

    @CommandLineInterface(application = "passive") interface ModePassive extends QuietOption {

    }

    @Command(description = "Set execution mode Passive")
    public boolean passive(ModePassive opts, CommandOutput output) throws IOException, InputError {
        return changeMode(opts, output, ExecutionMode.passive, RundeckApi::executionModeDisable);
    }

    boolean changeMode(
            final QuietOption opts,
            final CommandOutput output,
            final ExecutionMode expected,
            final Function<RundeckApi, Call<SystemMode>> operation
    )
            throws InputError, IOException
    {
        if (!opts.isQuiet()) {
            output.info(String.format("Setting execution mode to %s...", expected));
        }
        SystemMode mode = apiCall(operation);
        if (!opts.isQuiet()) {
            output.info("Execution Mode is now:");
            output.output(mode.getExecutionMode());
        }
        return expected.equals(mode.getExecutionMode());
    }

}
