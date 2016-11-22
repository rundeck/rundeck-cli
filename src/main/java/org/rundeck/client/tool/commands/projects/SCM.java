package org.rundeck.client.tool.commands.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.ANSIColorOutput;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import okhttp3.RequestBody;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ScmActionResult;
import org.rundeck.client.api.model.ScmConfig;
import org.rundeck.client.tool.commands.ApiCommand;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Colorz;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Created by greg on 7/21/16.
 */

@Command(description = "Manage Project SCM")
public class SCM extends ApiCommand {
    public SCM(final Supplier<Client<RundeckApi>> client) {
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
    public void config(ConfigOptions options, CommandOutput output) throws IOException {
        ScmConfig scmConfig1 = getClient().checkError(getClient().getService()
                                                                 .getScmConfig(options.getProject(), options.getIntegration()));

        HashMap<String, Object> basic = new HashMap<>();
        basic.put("Project", scmConfig1.project);
        basic.put("SCM Plugin type", scmConfig1.type);
        basic.put("SCM Plugin integration", scmConfig1.integration);
        output.output(Colorz.colorizeMapRecurse(basic, ANSIColorOutput.Color.GREEN));

        HashMap<String, Object> map = new HashMap<>();
        map.put("config", scmConfig1.config);
        if (options.isFile()) {
            //write to file
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(options.getFile(), map);
            output.info("Wrote config to file: " + options.getFile());
        } else {
            output.output(Colorz.colorizeMapRecurse(map, ANSIColorOutput.Color.GREEN, ANSIColorOutput.Color.YELLOW));

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
    public boolean setup(SetupOptions options, CommandOutput output) throws IOException {

        /*
         * body containing the file
         */
        RequestBody requestBody = RequestBody.create(
                Client.MEDIA_TYPE_JSON,
                options.getFile()
        );

        //dont use client.checkError, we want to handle 400 validation error
        Call<ScmActionResult> execute = getClient().getService()
                                                   .setupScmConfig(
                                                      options.getProject(),
                                                      options.getIntegration(),
                                                      options.getType(),
                                                      requestBody
                                              );

        Response<ScmActionResult> response = execute.execute();

        //check for 400 error with validation information
        checkValidationError(output, getClient(), response, options.getFile().getAbsolutePath());

        //otherwise check other error codes and fail if necessary
        ScmActionResult result = getClient().checkError(response);


        if (result.success) {
            output.info("Setup was successful.");
        } else {
            output.warning("Setup was not successful.");
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

    /**
     * Check for validation info from resposne
     *
     * @param output
     * @param client
     * @param response
     * @param filename
     *
     * @throws IOException
     */
    private static void checkValidationError(
            CommandOutput output,
            final Client<RundeckApi> client,
            final Response<ScmActionResult> response,
            final String filename
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
                        output.error("Setup config Validation failed for the file: ");
                        output.output(filename + "\n");
                        if (null != error.message) {
                            output.warning(error.message);
                        }

                        output.output(Colorz.colorizeMapRecurse(error.toMap(), ANSIColorOutput.Color.YELLOW));
                    }
                } catch (IOException e) {
                    //unable to parse body as expected
                    System.err.println("Expected SCM Validation error response, but was unable to parse it: " + e);
                }
                throw new RequestFailed(String.format(
                        "Setup config Validation failed: (error: %d %s)",
                        response.code(),
                        response.message()

                ), response.code(), response.message());
            }
        }
    }


}
