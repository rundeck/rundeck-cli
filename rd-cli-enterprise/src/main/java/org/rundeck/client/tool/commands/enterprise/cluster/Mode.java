package org.rundeck.client.tool.commands.enterprise.cluster;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import org.rundeck.client.api.model.ExecutionMode;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.commands.enterprise.BaseExtension;
import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi;
import org.rundeck.client.tool.commands.enterprise.api.model.EnterpriseModeResponse;
import org.rundeck.client.tool.extension.RdTool;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.BiFunction;

@CommandLine.Command(
        name = "mode",
        description = "Change Execution Mode of Cluster Members"
)
public class Mode extends BaseExtension {
    @Getter @Setter
    static class Options {
        @CommandLine.Option(names = {"-q", "--quiet"}, description = "Reduce output.")
        boolean quiet;
        @CommandLine.Option(names = {"-u", "--uuid"}, description = "Cluster member UUID", required = true)
        String uuid;
    }

    @CommandLine.Command(description = "Set cluster member execution mode Active")
    public boolean active(@CommandLine.Mixin Options options) throws IOException, InputError {
        return changeMode(ExecutionMode.active, options, EnterpriseApi::executionModeEnable);
    }


    @CommandLine.Command(description = "Set cluster member execution mode Passive")
    public boolean passive(@CommandLine.Mixin Options options) throws IOException, InputError {
        return changeMode(ExecutionMode.passive, options, EnterpriseApi::executionModeDisable);
    }

    boolean changeMode(
            final ExecutionMode expected,
            final Options options,
            final BiFunction<EnterpriseApi, String, Call<EnterpriseModeResponse>> operation
    )
            throws InputError, IOException {
        RdTool.apiVersionCheck("change cluster member execution mode", 41, getClient().getApiVersion());

        if (!options.isQuiet()) {
            getOutput().info(String.format("Setting Execution Mode to %s for cluster member %s...", expected, options.getUuid()));
        }

        EnterpriseModeResponse mode = getClient().apiCall((e) -> operation.apply(e, options.getUuid()));

        if (!options.isQuiet()) {
            getOutput().info(String.format("Execution Mode change is %s for cluster member %s:", mode.getStatus(), options.getUuid()));
            getOutput().output(mode.getExecutionMode());
        }

        return expected.equals(mode.getExecutionMode());
    }

}
