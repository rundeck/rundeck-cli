package org.rundeck.client.tool.commands.enterprise.cluster;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.api.model.ExecutionMode;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.commands.enterprise.BaseExtension;
import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi;
import org.rundeck.client.tool.commands.enterprise.api.model.EnterpriseModeResponse;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.SubCommand;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.BiFunction;

@Command()
@SubCommand(path = {"cluster"}, descriptions = {"Manage Rundeck Enterprise Cluster"})
public class Mode extends BaseExtension {

    interface QuietOption {
        @Option(shortName = "q", longName = "quiet", description = "Reduce output.")
        boolean isQuiet();
    }

    interface UuidOption {
        @Option(shortName = "u", longName = "uuid", description = "Cluster member UUID")
        String getUuid();
    }

    interface BaseOption extends QuietOption, UuidOption {
    }

    @CommandLineInterface(application = "active")
    interface ModeActive extends BaseOption {

    }

    @Command(description = "Set cluster member execution mode Active")
    public boolean active(ModeActive opts, CommandOutput output) throws IOException, InputError {
        return changeMode(opts, output, ExecutionMode.active, EnterpriseApi::executionModeEnable);
    }

    @CommandLineInterface(application = "passive")
    interface ModePassive extends BaseOption {

    }

    @Command(description = "Set cluster member execution mode Passive")
    public boolean passive(ModePassive opts, CommandOutput output) throws IOException, InputError {
        return changeMode(opts, output, ExecutionMode.passive, EnterpriseApi::executionModeDisable);
    }

    boolean changeMode(
            final BaseOption opts,
            final CommandOutput output,
            final ExecutionMode expected,
            final BiFunction<EnterpriseApi, String, Call<EnterpriseModeResponse>> operation
    )
            throws InputError, IOException {
        RdTool.apiVersionCheck("change cluster member execution mode", 41, getClient().getApiVersion());

        if (!opts.isQuiet()) {
            output.info(String.format("Setting Execution Mode to %s for cluster member %s...", expected, opts.getUuid()));
        }

        EnterpriseModeResponse mode = getClient().apiCall((e) -> operation.apply(e, opts.getUuid()));

        if (!opts.isQuiet()) {
            output.info(String.format("Execution Mode change is %s for cluster member %s:", mode.getStatus(), opts.getUuid()));
            output.output(mode.getExecutionMode());
        }

        return expected.equals(mode.getExecutionMode());
    }

}
