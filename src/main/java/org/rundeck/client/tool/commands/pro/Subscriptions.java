package org.rundeck.client.tool.commands.pro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.pro.Subscription;
import org.rundeck.client.api.model.pro.SubscriptionEventMessage;
import org.rundeck.client.api.model.pro.PluginValidationError;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.options.BaseOptions;
import org.rundeck.client.tool.options.ConfigFileOptions;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.client.tool.util.Colorz;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.toolbelt.ANSIColorOutput;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Command(description = "Manage Event Subscriptions")
public class Subscriptions
        extends AppCommand
{
    public Subscriptions(final RdApp rdApp) {
        super(rdApp);
    }

    interface ResultOptions
            extends VerboseOption, BaseOptions, ProjectNameOptions
    {

        @Option(shortName = "%",
                longName = "outformat",
                description = "Output format specifier for Subscription data. You can use \"%key\" where key is one of:"
                              +
                              "uuid, name, description, project, ")
        String getOutputFormat();

        boolean isOutputFormat();
    }

    @CommandLineInterface(application = "list")
    interface ListOpts
            extends ResultOptions
    {
    }

    @Command(description = "List Subscriptions for a Project.")
    public List<Subscription> list(ListOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        List<Subscription> result = apiCall(api -> api.listSubscriptions(
                project
        ));
        if (!options.isOutputFormat()) {
            output.info(String.format("%d Subscriptions in project %s%n", result.size(), project));
        }
        outputList(options, output, result, Subscription::asMap, Subscription::toBasicString);
        return result;
    }


    private <T> void outputList(
            final ResultOptions options,
            final CommandOutput output,
            final List<T> list,
            final Function<T, Map<?, ?>> verbose,
            final Function<T, String> basic
    )
    {
        final Function<T, ?> outformat;
        if (options.isVerbose()) {
            output.output(list.stream().map(verbose).collect(Collectors.toList()));
            return;
        }
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), verbose, "%", "");
        } else {
            outformat = basic;
        }

        output.output(list.stream().map(outformat).collect(Collectors.toList()));
    }

    interface SubscriptionId {
        @Option(shortName = "i", longName = "id", description = "Subscription ID")
        String getId();
    }

    @CommandLineInterface(application = "info")
    interface InfoOpts
            extends ResultOptions, SubscriptionId
    {

    }

    @Command(description = "Get Info for a Subscription.")
    public Subscription info(InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Subscription reactionList = apiCall(api -> api.getSubscriptionInfo(
                project,
                options.getId()
        ));
        output.output(reactionList);
        return reactionList;
    }

    @CommandLineInterface(application = "save")
    interface DownloadOpts
            extends VerboseOption, BaseOptions, ProjectNameOptions, SubscriptionId
    {

        @Option(shortName = "f",
                longName = "file",
                description = "File path of the file to download for storing the subscription (json format)")
        File getFile();
    }

    @Command(description = "Get Info for a Subscription.", synonyms = {"dl"})
    public void download(DownloadOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ResponseBody body = apiCall(api -> api.getSubscriptionInfoDownload(
                project,
                options.getId()
        ));
        Reactions.downloadResponseContent(output, body, options.getFile(), options.isVerbose());
    }

    @Command(description = "List Messages for a Subscription.")
    public List<SubscriptionEventMessage> messages(InfoOpts options, CommandOutput output)
            throws IOException, InputError
    {
        String project = projectOrEnv(options);
        List<SubscriptionEventMessage> messages = apiCall(api -> api.getSubscriptionMessages(
                project,
                options.getId()
        ));
        if (!options.isOutputFormat()) {
            output.info(String.format("%d Messages For Subscription %s%n", messages.size(), options.getId()));
        }
        outputList(options, output, messages, SubscriptionEventMessage::asMap, SubscriptionEventMessage::toBasicString);
        return messages;
    }

    interface ModifyOptions
            extends ConfigFileOptions
    {
        @Option(shortName = "t",
                longName = "type",
                description = "Subscription source type (optional if set in input file)")
        String getType();

        boolean isType();
    }

    @CommandLineInterface(application = "create")
    interface CreateOpts
            extends Reactions.ReactionResultOptions, ModifyOptions
    {
    }

    @Command(description = "Create a Subscription.")
    public boolean create(CreateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Subscription inputSubscription = readSubscription(options, project);
        ServiceClient.WithErrorResponse<Subscription> result = apiWithErrorResponse(api -> api.createSubscription(
                project,
                inputSubscription
        ));
        if (hasResponseValidationErrors(output, result, "Create Subscription")) {
            return false;
        }
        output.output(result.getResponse().body());
        return true;
    }

    /**
     * Output any validation errors with optional colorization
     *
     * @param output
     * @param message
     * @param errors
     * @param ansiEnabled
     */
    public static void outputValidationErrors(
            final CommandOutput output,
            final String message,
            final Map<String, ?> errors,
            final boolean ansiEnabled
    )
    {
        if (null != message) {
            output.warning(message);
        }
        Optional<? extends Map<?, ?>> errorData = Optional.ofNullable(errors);
        errorData.ifPresent(map -> output.output(
                ansiEnabled ?
                Colorz.colorizeMapRecurse(
                        map,
                        ANSIColorOutput.Color.YELLOW
                ) : map
        ));
    }


    /**
     * Load validation data from the error response if it is a 400 response
     * @param serviceClient
     * @param errorResponse
     * @param name
     * @param clazz validation class
     * @param <Z> validation class
     * @param <T> data class
     */
    public static <Z, T> Optional<Z> getValidationError(
            final ServiceClient<RundeckApi> serviceClient,
            final ServiceClient.WithErrorResponse<T> errorResponse,
            final String name,
            Class<Z> clazz
    )
    {
        Response<T> response = errorResponse.getResponse();
        if (errorResponse.isError400()) {
            try {
                //parse body as Subscription
                Z error = serviceClient.readError(errorResponse.getErrorBody(), clazz, Client.MEDIA_TYPE_JSON);
                return Optional.ofNullable(error);
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
        return Optional.empty();
    }

    @CommandLineInterface(application = "update")
    interface UpdateOpts
            extends ResultOptions, ModifyOptions, SubscriptionId
    {
    }

    @Command(description = "Update a Subscription")
    public boolean update(UpdateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Subscription input = readSubscription(options, project);
        ServiceClient.WithErrorResponse<Subscription>
                response =
                apiWithErrorResponse(api -> api.updateSubscription(project, options.getId(), input));

        if (hasResponseValidationErrors(output, response, "Update Subscription [" + options.getId() + "]")) {
            return false;
        }
        output.info(String.format("Updated Subscription: %s", options.getId()));
        output.output(response.getResponse().body());
        return true;
    }

    /**
     * Check if the response is a 500 error with validation information
     *
     * @param output
     * @param response
     * @param name     operation description
     * @return true if there were validation errors present
     * @throws InputError
     */
    public boolean hasResponseValidationErrors(
            final CommandOutput output,
            final ServiceClient.WithErrorResponse<Subscription> response,
            final String name
    ) throws InputError
    {
        Optional<PluginValidationError>
                hasValidationError =
                getValidationError(getClient(), response, name, PluginValidationError.class);
        hasValidationError.ifPresent(pluginValidationError -> {
            PluginValidationError validation = hasValidationError.get();
            output.error("Failed: " + name);
            outputValidationErrors(
                    output,
                    validation.getMessage(),
                    validation.getErrors(),
                    getAppConfig().isAnsiEnabled()
            );
        });
        return hasValidationError.isPresent();
    }

    public Subscription readSubscription(final ModifyOptions options, final String project)
            throws IOException, InputError
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Subscription input = objectMapper.readValue(options.getFile(), Subscription.class);

        input.setProject(project);

        if (options.isType()) {
            input.setType(options.getType());
        } else if (input.getType() == null) {
            throw new InputError("Expected -t/--type arg, or json file to have a \"type\" value");
        }
        return input;
    }

    @CommandLineInterface(application = "enable")
    interface EnableOpts
            extends ResultOptions, SubscriptionId
    {

    }

    @Command(description = "Enable a Subscription")
    public Subscription enable(EnableOpts options, CommandOutput output) throws IOException, InputError {
        Subscription reaction = updateUsing(
                projectOrEnv(options),
                options.getId(),
                sub -> sub.setEnabled(true)
        );
        output.info(String.format("Enabled Subscription: %s", options.getId()));
        output.output(reaction);
        return reaction;
    }

    @CommandLineInterface(application = "disable")
    interface DisableOpts
            extends ResultOptions, SubscriptionId
    {

    }

    @Command(description = "Disable a Subscription")
    public Subscription disable(DisableOpts options, CommandOutput output) throws IOException, InputError {
        Subscription reaction = updateUsing(
                projectOrEnv(options),
                options.getId(),
                sub -> sub.setEnabled(false)
        );
        output.info(String.format("Disabled Subscription: %s", options.getId()));
        output.output(reaction);
        return reaction;
    }

    private Subscription updateUsing(final String project, final String id, final Consumer<Subscription> consumer)
            throws InputError, IOException
    {
        Subscription inputSubscription = new Subscription();
        consumer.accept(inputSubscription);
        return apiCall(api -> api.updateSubscription(project, id, inputSubscription));
    }


    @Command(description = "Delete a Subscription")
    public void delete(Subscriptions.InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Void x = apiCall(api -> api.deleteSubscription(
                project,
                options.getId()
        ));
        output.info(String.format("Deleted subscription: %s", options.getId()));
    }
}
