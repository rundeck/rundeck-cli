package org.rundeck.client.tool.commands.system;


import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ExecutionMode;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.api.model.SystemMode;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.QuietOption;
import picocli.CommandLine;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author greg
 * @since 8/14/17
 */

@CommandLine.Command(description = "Manage Execution Mode", name = "mode")
public class Mode extends BaseCommand {


    @Getter @Setter
    static
    class ModeInfo {

        @CommandLine.Option(names = {"-A", "--testactive"},
                description = "Test whether the execution mode is active: fail if not")
        boolean testActive;

        @CommandLine.Option(names = {"-P", "--testpassive"},
                description = "Test whether the execution mode is passive: fail if not")
        boolean testPassive;
    }

    @CommandLine.Command(description =
            "Show execution mode\n" +
                    "When --testactive or --testpassive are used, the exit code will be 0 if the test is successful, 1 otherwise.")
    public int info(@CommandLine.Mixin ModeInfo opts) throws IOException, InputError {
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
            getRdOutput().warning(message);
        } else {
            getRdOutput().info(message);
        }
        getRdOutput().output(executionMode);
        return testpass ? 0 : 1;
    }


    @CommandLine.Command(description = "Set execution mode Active")
    public int active(@CommandLine.Mixin QuietOption opts) throws IOException, InputError {
        return changeMode(opts, ExecutionMode.active, RundeckApi::executionModeEnable) ? 0 : 1;
    }


    @CommandLine.Command(description = "Set execution mode Passive")
    public int passive(@CommandLine.Mixin QuietOption opts) throws IOException, InputError {
        return changeMode(opts, ExecutionMode.passive, RundeckApi::executionModeDisable) ? 0 : 1;
    }

    boolean changeMode(
            final QuietOption opts,
            final ExecutionMode expected,
            final Function<RundeckApi, Call<SystemMode>> operation
    )
            throws InputError, IOException {
        if (!opts.isQuiet()) {
            getRdOutput().info(String.format("Setting execution mode to %s...", expected));
        }
        SystemMode mode = apiCall(operation);
        if (!opts.isQuiet()) {
            getRdOutput().info("Execution Mode is now:");
            getRdOutput().output(mode.getExecutionMode());
        }
        return expected.equals(mode.getExecutionMode());
    }

}
