/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.OptionUtil;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.client.util.Client;
import org.rundeck.client.tool.util.Colorz;
import org.rundeck.client.util.ServiceClient;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * scm subcommands
 */

@Command(description = "Manage Project SCM")
public class SCM extends AppCommand {
    public SCM(final RdApp client) {
        super(client);
    }

    public interface BaseScmOptions extends ProjectNameOptions {

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
        String project = projectOrEnv(options);
        ScmConfig scmConfig1 = apiCall(api -> api.getScmConfig(project, options.getIntegration()));

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
        String project = projectOrEnv(options);

        //get response to handle 400 validation error
        ServiceClient.WithErrorResponse<ScmActionResult> response = apiWithErrorResponse(api -> api
                .setupScmConfig(
                        project,
                        options.getIntegration(),
                        options.getType(),
                        requestBody
                ));

        //check for 400 error with validation information
        if (hasValidationError(
                output,
                getClient(),
                response,
                "Setup config Validation for file: " + options.getFile().getAbsolutePath(),
                getAppConfig().isAnsiEnabled()
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
        String project = projectOrEnv(options);
        ScmProjectStatusResult result = apiCall(api -> api.getScmProjectStatus(
                project,
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
        String project = projectOrEnv(options);
        Void result = apiCall(api -> api.enableScmPlugin(
                project,
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
        String project = projectOrEnv(options);
        Void result = apiCall(api -> api.disableScmPlugin(
                project,
                options.getIntegration(),
                options.getType()
        ));

    }

    @CommandLineInterface(application = "setupinputs")
    public interface InputsOptions extends BaseScmOptions, VerboseOption {
        @Option(longName = "type", shortName = "t", description = "Plugin type")
        String getType();
    }

    @Command(description = "Get SCM Setup inputs")
    public void setupinputs(InputsOptions options, CommandOutput output) throws IOException, InputError {

        String project = projectOrEnv(options);
        //otherwise check other error codes and fail if necessary
        ScmSetupInputsResult result = apiCall(api -> api.getScmSetupInputs(
                project,
                options.getIntegration(),
                options.getType()
        ));

        if (options.isVerbose()) {
            output.output(result);
            return;
        }

        output.output(result.fields.stream().map(ScmInputField::asMap).collect(Collectors.toList()));
    }

    @CommandLineInterface(application = "inputs")
    public interface ActionInputsOptions extends BaseScmOptions, VerboseOption {
        @Option(longName = "action", shortName = "a", description = "Action ID")
        String getAction();

    }

    @Command(description = "Get SCM action inputs")
    public void inputs(ActionInputsOptions options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);

        ScmActionInputsResult result = apiCall(api -> api.getScmActionInputs(
                project,
                options.getIntegration(),
                options.getAction()
        ));

        if (options.isVerbose()) {
            output.output(result);
            return;
        }
        output.output(result.title + ": " + result.description);
        output.output("Fields:");
        output.output(result.fields.stream().map(ScmInputField::asMap).collect(Collectors.toList()));
        output.output("Items:");
        if ("export".equals(options.getIntegration())) {
            output.output(result.exportItems.stream().map(ScmExportItem::asMap).collect(Collectors.toList()));
        } else {
            output.output(result.importItems.stream().map(ScmImportItem::asMap).collect(Collectors.toList()));
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

        @Option(shortName = "A",
                longName = "allitems",
                description = "Include all items from the result of calling Inputs in export or import action")
        boolean isAllItems();

        @Option(shortName = "M",
                longName = "allmodified",
                description = "Include all modified (not deleted) items from the result of calling Inputs in Export " +
                              "action (export only)")
        boolean isAllModifiedItems();

        @Option(shortName = "D",
                longName = "alldeleted",
                description = "Include all deleted items from the result of calling Inputs in Export " +
                              "action (export only)")
        boolean isAllDeletedItems();

        @Option(shortName = "T",
                longName = "alltracked",
                description = "Include all tracked (not new) items from the result of calling Inputs in Import action" +
                              " (import only)")
        boolean isAllTrackedItems();

        @Option(shortName = "U",
                longName = "alluntracked",
                description = "Include all untracked (new) items from the result of calling Inputs in Import action " +
                              "(import only)")
        boolean isAllUntrackedItems();

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
        String project = projectOrEnv(options);
        String integration = options.getIntegration();
        boolean export = "export".equals(integration);
        if (options.isAllItems() ||
            export && (options.isAllDeletedItems() || options.isAllModifiedItems()) ||
            !export && (options.isAllTrackedItems() || options.isAllUntrackedItems())) {
            //call the Inputs endpoint to list the items for the action
            ScmActionInputsResult inputs = apiCall(api -> api.getScmActionInputs(
                    project,
                    integration,
                    options.getAction()
            ));
            if (export) {
                List<ScmExportItem> exportItems = inputs.exportItems;
                if (options.isAllItems() || options.isAllModifiedItems()) {
                    perform.setItems(exportItems.stream()
                                                .filter(a -> !a.getDeleted())
                                                .map(a -> a.itemId)
                                                .collect(Collectors.toList()));
                }
                if (options.isAllItems() || options.isAllDeletedItems()) {
                    perform.setDeleted(exportItems.stream()
                                                  .filter(ScmExportItem::getDeleted)
                                                  .map(a -> a.itemId)
                                                  .collect(Collectors.toList()));
                }
            } else {
                List<ScmImportItem> importItems = inputs.importItems;
                perform.setItems(importItems.stream()
                                            .filter(a -> options.isAllItems() ||
                                                         options.isAllTrackedItems() && a.tracked ||
                                                         options.isAllUntrackedItems() && !a.tracked)
                                            .map(a -> a.itemId)
                                            .collect(Collectors.toList()));
            }
        }
        ServiceClient.WithErrorResponse<ScmActionResult> response = apiWithErrorResponse(api -> api.performScmAction(
                project,
                options.getIntegration(),
                options.getAction(),
                perform
        ));

        //check for 400 error with validation information
        if (hasValidationError(
                output,
                getClient(),
                response,
                "Action " + options.getAction(),
                getAppConfig().isAnsiEnabled()
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
        String project = projectOrEnv(options);
        ScmPluginsResult result = apiCall(api -> api.listScmPlugins(project, options.getIntegration()));
        output.output(result.plugins.stream().map(ScmPlugin::toMap).collect(Collectors.toList()));
    }

    /**
     * Check for validation info from resposne
     *
     * @param name action name for error messages
     * @param colorize
     */
    private static boolean hasValidationError(
            CommandOutput output,
            final ServiceClient<RundeckApi> serviceClient,
            final ServiceClient.WithErrorResponse<ScmActionResult> errorResponse,
            final String name, final boolean colorize
    )
    {
        Response<ScmActionResult> response = errorResponse.getResponse();
        if (errorResponse.isError400()) {
            try {
                //parse body as ScmActionResult
                ScmActionResult error = serviceClient.readError(
                        errorResponse.getErrorBody(),
                        ScmActionResult.class,
                        Client.MEDIA_TYPE_JSON
                );
                if (null != error) {
                    //
                    output.error(String.format("%s failed", name));
                    if (null != error.message) {
                        output.warning(error.message);
                    }
                    Optional<? extends Map<?, ?>> errorData = Optional.ofNullable(error.toMap());
                    errorData.ifPresent(map -> output.output(
                            colorize ?
                            Colorz.colorizeMapRecurse(
                                    map,
                                    ANSIColorOutput.Color.YELLOW
                            ) : map
                    ));
                }
            } catch (IOException e) {
                //unable to parse body as expected
                e.printStackTrace();
                throw new RequestFailed(String.format(
                        "%s failed: (error: %d %s)",
                        name,
                        response.code(),
                        response.message()

                ), response.code(), response.message());
            }

        }
        return !response.isSuccessful();
    }


}
