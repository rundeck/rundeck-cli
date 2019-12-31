package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ExecutionModeLaterResponse;
import org.rundeck.client.api.model.ProjectExecutionModeLater;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.options.ProjectExecutionModeLaterOptions;
import org.rundeck.client.tool.options.QuietOption;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;
import retrofit2.Response;

import java.io.IOException;

/**
 * Subcommands for project enable/disable execution/schedule after X time
 *
 * @author roberto
 * @since 12/30/19
 */
@Command(description = "Manage Project Enable/Disable execution/schedule after X time")
public class Mode extends AppCommand {
    public Mode(final RdApp rdApp) {
        super(rdApp);
    }


    @CommandLineInterface(application = "enableLater") interface ModeActiveLater extends ProjectExecutionModeLaterOptions, QuietOption {

    }

    @Command(description = "enable executions/schedule mode later")
    public boolean enableLater(ModeActiveLater opts, CommandOutput output) throws IOException, InputError {

        ProjectExecutionModeLater enableLater = new ProjectExecutionModeLater();
        enableLater.setValue(opts.getTimeValue());
        enableLater.setType(opts.getType());

        String project = opts.getProject();

        if (!opts.isQuiet()) {
            output.info(String.format("Setting %s mode to  %s", opts.getType() , opts.getTimeValue()));
        }

        ServiceClient.WithErrorResponse<ExecutionModeLaterResponse> execute = apiWithErrorResponseDowngradable(
                getRdApp(),
                api -> api.projectExecutionModeEnableLater(project,enableLater)
        );

        checkValidationError(output,getRdApp().getClient(),execute);

        ExecutionModeLaterResponse response = getRdApp().getClient().checkError(execute);

        if (!opts.isQuiet()) {
            if(response.isSaved()){
                output.info(String.format("%s will be enable after %s", opts.getType() , opts.getTimeValue()));
                output.output(response.getMsg());
            }else{
                output.warning(String.format("%s mode wasn't save", opts.getType() ));
                output.output(response.getMsg());
            }
        }

        return response.isSaved();
    }

    @Command(description = "disable executions/schedule mode later")
    public boolean disableLater(ModeActiveLater opts, CommandOutput output) throws IOException, InputError {

        ProjectExecutionModeLater disableLater = new ProjectExecutionModeLater();
        disableLater.setValue(opts.getTimeValue());
        disableLater.setType(opts.getType());

        String project = opts.getProject();

        if (!opts.isQuiet()) {
            output.info(String.format("Setting %s mode to  %s", opts.getType() , opts.getTimeValue()));
        }

        ServiceClient.WithErrorResponse<ExecutionModeLaterResponse> execute = apiWithErrorResponseDowngradable(
                getRdApp(),
                api -> api.projectExecutionModeDisableLater(project,disableLater)
        );

        checkValidationError(output,getRdApp().getClient(),execute);

        ExecutionModeLaterResponse response = getRdApp().getClient().checkError(execute);

        if (!opts.isQuiet()) {
            if(response.isSaved()){
                output.info(String.format("%s will be disable after %s", opts.getType() , opts.getTimeValue()) );
                output.output(response.getMsg());
            }else{
                output.warning(String.format("%s mode wasn't save", opts.getType() ));
                output.output(response.getMsg());
            }
        }

        return response.isSaved();
    }

    private static void checkValidationError(
            CommandOutput output,
            final ServiceClient<RundeckApi> client,
            final ServiceClient.WithErrorResponse<ExecutionModeLaterResponse> errorResponse
    )
            throws IOException
    {
        Response<ExecutionModeLaterResponse> response = errorResponse.getResponse();
        if (errorResponse.isError400()) {
            ExecutionModeLaterResponse error = client.readError(
                    errorResponse.getErrorBody(),
                    ExecutionModeLaterResponse.class,
                    Client.MEDIA_TYPE_JSON
            );

            if (null != error) {
                output.error(error.getMsg());
            }

            throw new RequestFailed(String.format(
                    "Validation failed: (error: %d %s)",
                    response.code(),
                    response.message()
            ), response.code(), response.message());
        }
    }
}
