package org.rundeck.client.tool.commands.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.ANSIColorOutput;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.RequestBody;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.*;
import org.rundeck.client.tool.commands.ApiCommand;
import org.rundeck.client.tool.options.OptionUtil;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Colorz;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by greg on 7/21/16.
 */

@Command(description = "Manage Project SCM")
public class SCM extends ApiCommand {
    public SCM(final HasClient client) {
        super(client);
    }

    public interface BaseScmOptions {
        @Option(longName = "project", shortName = "p", description = "Project name")
        String getProject();

        @Option(longName = "integration",
                shortName = "i",
                description = "Integration type (export/import)",
                pattern = "^(import|export)$")
        String getIntegration();
    }

    @CommandLineInterface(application = "config")
    public interface ConfigOptions extends BaseScmOptions {

        @Option(longName = "file", shortName = "f", description = "If specified, write config to a file (json format)")
        File getFile();

        boolean isFile();
    }

    @Command(description = "Get SCM Config for a Project")
    public void config(ConfigOptions options, CommandOutput output) throws IOException, InputError {
        ScmConfig scmConfig1 = apiCall(api -> api.getScmConfig(options.getProject(), options.getIntegration()));

        HashMap<String, Object> basic = new HashMap<>();
        basic.put("Project", scmConfig1.project);
        basic.put("SCM Plugin type", scmConfig1.type);
        basic.put("SCM Plugin integration", scmConfig1.integration);
        output.info(basic);

        HashMap<String, Object> map = new HashMap<>();
        map.put("config", scmConfig1.config);
        if (options.isFile()) {
            //write to file
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(options.getFile(), map);
            output.info("Wrote config to file: " + options.getFile());
        } else {
            output.output(map);
        }
    }

    @CommandLineInterface(application = "setup")
    public interface SetupOptions extends BaseScmOptions {
        @Option(longName = "type", shortName = "t", description = "Plugin type")
        String getType();

        @Option(longName = "file", shortName = "f", description = "Config file (json format)")
        File getFile();
    }

    @Command(description = "Setup SCM Config for a Project")
    public boolean setup(SetupOptions options, CommandOutput output) throws IOException, InputError {

        /*
         * body containing the file
         */
        RequestBody requestBody = RequestBody.create(
                Client.MEDIA_TYPE_JSON,
                options.getFile()
        );

        //dont use client.checkError, we want to handle 400 validation error

        Response<ScmActionResult> response = getClient().getService()
                                                        .setupScmConfig(
                                                           options.getProject(),
                                                           options.getIntegration(),
                                                           options.getType(),
                                                           requestBody
                                                        ).execute();

        //check for 400 error with validation information
        if (!checkValidationError(output, getClient(), response,
                                  "Setup config Validation for file: " + options.getFile().getAbsolutePath()
        )) {
            return false;
        }

        //otherwise check other error codes and fail if necessary
        ScmActionResult result = getClient().checkError(response);


        return outputScmActionResult(output, result, "Setup");
    }

    private boolean outputScmActionResult(final CommandOutput output, final ScmActionResult result, final String name) {
        if (result.success) {
            output.info(name + " was successful.");
        } else {
            output.warning(name + " was not successful.");
        }
        if (result.message != null) {
            output.info("Result: " + result.message);
        }
        if (result.nextAction != null) {
            output.output(ANSIColorOutput.colorize(
                    "Next Action: ",
                    ANSIColorOutput.Color.GREEN,
                    result.nextAction
            ));
        }

        return result.success;
    }

    @CommandLineInterface(application = "status")
    public interface StatusOptions extends BaseScmOptions {
    }

    @Command(description = "Get SCM Status for a Project")
    public boolean status(StatusOptions options, CommandOutput output) throws IOException, InputError {
        ScmProjectStatusResult result = apiCall(api -> api.getScmProjectStatus(
                options.getProject(),
                options.getIntegration()
        ));


        output.output(result.toMap());
        return result.synchState == ScmSynchState.CLEAN;
    }

    @CommandLineInterface(application = "enable")
    public interface EnableOptions extends BaseScmOptions {
        @Option(longName = "type", shortName = "t", description = "Plugin type")
        String getType();
    }

    @Command(description = "Enable plugin ")
    public void enable(EnableOptions options, CommandOutput output) throws IOException, InputError {
        //otherwise check other error codes and fail if necessary
        Void result = apiCall(api -> api.enableScmPlugin(
                options.getProject(),
                options.getIntegration(),
                options.getType()
        ));

    }

    @CommandLineInterface(application = "disable")
    public interface DisableOptions extends EnableOptions {
    }

    @Command(description = "Disable plugin ")
    public void disable(DisableOptions options, CommandOutput output) throws IOException, InputError {
        //otherwise check other error codes and fail if necessary
        Void result = apiCall(api -> api.disableScmPlugin(
                options.getProject(),
                options.getIntegration(),
                options.getType()
        ));

    }

    @CommandLineInterface(application = "setupinputs")
    public interface InputsOptions extends BaseScmOptions {
        @Option(longName = "type", shortName = "t", description = "Plugin type")
        String getType();
    }

    @Command(description = "Get SCM Setup inputs")
    public void setupinputs(InputsOptions options, CommandOutput output) throws IOException, InputError {


        //otherwise check other error codes and fail if necessary
        ScmSetupInputsResult result = apiCall(api -> api.getScmSetupInputs(
                options.getProject(),
                options.getIntegration(),
                options.getType()
        ));

        output.output(result.fields.stream().map(ScmInputField::toMap).collect(Collectors.toList()));
    }

    @CommandLineInterface(application = "setupinputs")
    public interface ActionInputsOptions extends BaseScmOptions {
        @Option(longName = "action", shortName = "a", description = "Action ID")
        String getAction();

    }

    @Command(description = "Get SCM action inputs")
    public void inputs(ActionInputsOptions options, CommandOutput output) throws IOException, InputError {


        ScmActionInputsResult result = apiCall(api -> api.getScmActionInputs(
                options.getProject(),
                options.getIntegration(),
                options.getAction()
        ));

        output.output(result.title + ": " + result.description);
        output.output("Fields:");
        output.output(result.fields.stream().map(ScmInputField::toMap).collect(Collectors.toList()));
        output.output("Items:");
        if ("export".equals(options.getIntegration())) {
            output.output(result.exportItems.stream().map(ScmExportItem::toMap).collect(Collectors.toList()));
        } else {
            output.output(result.importItems.stream().map(ScmImportItem::toMap).collect(Collectors.toList()));
        }
    }

    @CommandLineInterface(application = "perform")
    public interface ActionPerformOptions extends BaseScmOptions {
        @Option(longName = "action", shortName = "a", description = "Action ID")
        String getAction();

        @Option(longName = "field", shortName = "f", description = "Field input values, space separated key=value list")
        List<String> getFields();

        boolean isFields();

        @Option(longName = "item", shortName = "I", description = "Items to include, space separated list")
        List<String> getItem();

        boolean isItem();

        @Option(longName = "job", shortName = "j", description = "Job IDs to include, space separated list")
        List<String> getJob();

        boolean isJob();

        @Option(longName = "delete",
                shortName = "d",
                description = "Job IDs or Item Ids to delete, space separated list")
        List<String> getDelete();

        boolean isDelete();
    }

    @Command(description = "Perform SCM action")
    public boolean perform(ActionPerformOptions options, CommandOutput output) throws IOException, InputError {

        ScmActionPerform perform = performFromOptions(options);
        Response<ScmActionResult> response = getClient().getService().performScmAction(
                options.getProject(),
                options.getIntegration(),
                options.getAction(),
                perform
        ).execute();

        //check for 400 error with validation information
        if (!checkValidationError(output, getClient(), response,
                                  "Action " + options.getAction()
        )) {
            return false;
        }

        //otherwise check other error codes and fail if necessary
        ScmActionResult result = getClient().checkError(response);
        return outputScmActionResult(output, result, "Action " + options.getAction());
    }

    private ScmActionPerform performFromOptions(final ActionPerformOptions options) throws InputError {
        ScmActionPerform perform = new ScmActionPerform();
        if (null != options.getFields()) {
            perform.setInput(OptionUtil.parseKeyValueMap(options.getFields(), null, "="));
        } else {
            perform.setInput(new HashMap<>());
        }
        List<String> item = options.getItem();
        perform.setItems(item != null ? item : new ArrayList<>());
        List<String> job = options.getJob();
        perform.setJobs(job != null ? job : new ArrayList<>());
        List<String> delete = options.getDelete();
        perform.setDeleted(delete != null ? delete : new ArrayList<>());
        return perform;
    }

    @CommandLineInterface(application = "plugins")
    public interface ListPluginsOptions extends BaseScmOptions {

    }

    @Command(description = "List SCM plugins")
    public void plugins(ListPluginsOptions options, CommandOutput output) throws IOException, InputError {


        //dont use client.checkError, we want to handle 400 validation error


        //otherwise check other error codes and fail if necessary
        ScmPluginsResult result = apiCall(api -> api.listScmPlugins(options.getProject(), options.getIntegration()));
        output.output(result.plugins.stream().map(ScmPlugin::toMap).collect(Collectors.toList()));
    }

    /**
     * Check for validation info from resposne
     *
     * @param output
     * @param client
     * @param response
     * @param name
     * @throws IOException
     */
    private static boolean checkValidationError(
            CommandOutput output,
            final Client<RundeckApi> client,
            final Response<ScmActionResult> response,
            final String name
    )
            throws IOException
    {
        if (!response.isSuccessful()) {
            if (response.code() == 400) {
                try {
                    //parse body as ScmActionResult
                    ScmActionResult error = client.readError(
                            response,
                            ScmActionResult.class,
                            Client.MEDIA_TYPE_JSON
                    );
                    if (null != error) {
                        //
                        output.error(name + " failed");
                        if (null != error.message) {
                            output.warning(error.message);
                        }
                        Optional<? extends Map<?, ?>> errorData = Optional.ofNullable(error.toMap());
                        errorData.ifPresent(map -> {
                            output.output(Colorz.colorizeMapRecurse(map, ANSIColorOutput.Color.YELLOW));
                        });
                    }
                } catch (IOException e) {
                    //unable to parse body as expected
                    throw new RequestFailed(String.format(
                            name + " failed: (error: %d %s)",
                            response.code(),
                            response.message()

                    ), response.code(), response.message());
                }

            }
        }
        return response.isSuccessful();
    }


}
