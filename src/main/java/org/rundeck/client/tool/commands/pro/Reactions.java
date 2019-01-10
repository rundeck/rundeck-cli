package org.rundeck.client.tool.commands.pro;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.pro.Reaction;
import org.rundeck.client.api.model.pro.ReactionEvent;
import org.rundeck.client.api.model.pro.ReactionEventList;
import org.rundeck.client.api.model.pro.ReactionValidationError;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.*;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(description = "Manage Reactions")
public class Reactions
        extends AppCommand
{
    public Reactions(final RdApp rdApp) {
        super(rdApp);
    }

    interface ReactionResultOptions
            extends VerboseOption, BaseOptions, ProjectNameOptions
    {

        @Option(shortName = "%",
                longName = "outformat",
                description = "Output format specifier for reaction data. You can use \"%key\" where key is one of:"
                              +
                              "uuid, name, description, project, ")
        String getOutputFormat();

        boolean isOutputFormat();
    }

    @CommandLineInterface(application = "list")
    interface ListOpts
            extends ReactionResultOptions
    {
    }

    @Command(description = "List reactions for a project. Use -p/--project or RD_PROJECT env var to specify project.")
    public List<Reaction> list(ListOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        List<Reaction> result = apiCall(api -> api.listReactions(
                project
        ));
        if (!options.isOutputFormat()) {
            output.info(String.format("%d Reactions in project %s%n", result.size(), project));
        }
        outputList(options, output, result);
        return result;
    }

    private void outputList(
            final ReactionResultOptions options,
            final CommandOutput output,
            final List<Reaction> list
    )
    {
        final Function<Reaction, ?> outformat;
        if (options.isVerbose()) {
            output.output(list.stream().map(Reaction::asMap).collect(Collectors.toList()));
            return;
        }
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), Reaction::asMap, "%", "");
        } else {
            outformat = Reaction::toBasicString;
        }

        output.output(list.stream().map(outformat).collect(Collectors.toList()));
    }

    interface ReactionId

    {

        @Option(shortName = "i", longName = "id", description = "Reaction ID")
        String getId();
    }

    @CommandLineInterface(application = "info")
    interface InfoOpts
            extends ReactionResultOptions, ReactionId
    {
    }


    @Command(description = "Get Info about a Reaction. Use -i/--id to specify ID.")
    public Reaction info(InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Reaction reactionList = apiCall(api -> api.getReactionInfo(
                project,
                options.getId()
        ));
        output.output(reactionList);
        return reactionList;
    }

    @CommandLineInterface(application = "download")
    interface DownloadOpts
            extends VerboseOption, BaseOptions, ProjectNameOptions, ReactionId
    {
        @Option(shortName = "f",
                longName = "file",
                description = "File path of the file to download for storing the reaction (json format)")
        File getFile();
    }

    @Command(description = "Download a Reaction defintion. Use -i/--id to specify ID.", synonyms = {"dl"})
    public void download(DownloadOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ResponseBody body = apiCall(api -> api.getReactionInfoDownload(
                project,
                options.getId()
        ));
        downloadResponseContent(output, body, options.getFile(), options.isVerbose());
    }

    public static void downloadResponseContent(
            final CommandOutput output,
            final ResponseBody body,
            final File file,
            final boolean verbose
    ) throws IOException
    {
        if (!ServiceClient.hasAnyMediaType(body.contentType(), Client.MEDIA_TYPE_JSON)) {
            throw new IllegalStateException("Unexpected response format: " + body.contentType());
        }
        InputStream inputStream = body.byteStream();
        if ("-".equals(file.getName())) {
            Util.copyStream(inputStream, System.out);
        } else {
            try (FileOutputStream out = new FileOutputStream(file)) {
                long total = Util.copyStream(inputStream, out);
                if (verbose) {
                    output.info(String.format(
                            "Wrote %d bytes of %s to file %s%n",
                            total,
                            body.contentType(),
                            file
                    ));
                }
            }
        }
    }

    public interface EventsFormatOptions {

        @Option(shortName = "%",
                longName = "outformat",
                description = "Output format specifier for event data. You can use \"%key\" where key is one of:"
                              +
                              "uuid, project, status, dateCreated*, event*, subscription*, reaction*, source*, result* "
                              +
                              ". Some data has extended fields, like event.type E.g. \"%status %event.type %dateCreated.relative\"")
        String getOutputFormat();

        boolean isOutputFormat();
    }

    public interface EventsResultOptions
            extends EventsFormatOptions, VerboseOption
    {

    }

    @CommandLineInterface(application = "events")
    interface EventsOpts
            extends ReactionResultOptions, ReactionId, PagingResultOptions, EventsResultOptions
    {
    }


    @Command(description = "List the events for the reaction")
    public ReactionEventList events(EventsOpts options, CommandOutput output) throws IOException, InputError {
        int offset = options.isOffset() ? options.getOffset() : 0;
        int max = options.isMax() ? options.getMax() : 20;

        String project = projectOrEnv(options);
        ReactionEventList eventList = apiCall(api -> api.getReactionEvents(
                project,
                options.getId(),
                offset,
                max
        ));
        if (!options.isOutputFormat()) {
            output.info(String.format("Reaction has %d Events%n", eventList.getPagination().getCount()));
        }

        outputEventsList(options, output, getAppConfig(), eventList.getEvents().stream());
        return eventList;
    }

    public static void outputEventsList(
            final EventsResultOptions options,
            final CommandOutput out,
            final RdClientConfig config,
            final Stream<ReactionEvent> executions
    )
    {
        if (options.isVerbose()) {
            out.output(executions.map(ReactionEvent::asMap).collect(Collectors.toList()));
            return;
        }
        final Function<ReactionEvent, ?> outformat;
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), ReactionEvent::asMap, "%", "");
        } else {
            outformat = e -> e.toExtendedString(config);
        }
        executions.forEach(e -> out.output(outformat.apply(e)));
    }

    interface ToggleOptions {
        @Option(shortName = "e", longName = "enable", description = "Enable the reaction")
        boolean isEnable();

        @Option(shortName = "d", longName = "disable", description = "Disable the reaction")
        boolean isDisable();

        @Option(shortName = "a",
                longName = "activation",
                description = "Set the enabled/disabled the reaction, one of: enabled,disabled")
        Activation getActivation();

        boolean isActivation();
    }

    static enum Activation {
        enabled(true),
        disabled(false);

        private boolean active;

        Activation(final boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }

    interface ModifyReactionOptions
            extends ToggleOptions
    {

        @Option(shortName = "n", longName = "name", description = "Reaction name")
        String getName();

        boolean isName();

        @Option(shortName = "d", longName = "description", description = "Reaction description (optional)")
        String getDescription();

        boolean isDescription();

        @Option(shortName = "f",
                longName = "file",
                description = "Input file for Reaction (json format)")
        File getFile();


    }

    @CommandLineInterface(application = "create")
    interface CreateOpts
            extends ReactionResultOptions, ModifyReactionOptions
    {


    }

    @Command(description = "Create a Reaction. Use -f/--file to specify data file")
    public boolean create(CreateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);

        final Reaction inputReaction = readReactionInput(options);

        if (null == inputReaction.getName()) {
            throw new InputError("-n/--name arg, or \"name\" entry in json is required for create");
        }
        if (null == inputReaction.getSelector()) {
            throw new InputError("\"selector\" entry in json is required for create");
        }
        if (null == inputReaction.getHandlers() || inputReaction.getHandlers().size() < 1) {
            throw new InputError("\"handlers\" entry in json is required for create");
        }

        ServiceClient.WithErrorResponse<Reaction> response = apiWithErrorResponse(api -> api.createReaction(
                project,
                inputReaction
        ));
        if (hasValidationErrors(output, response, "Create Reaction")) {
            return false;
        }
        Reaction reaction = response.getResponse().body();
        output.info(String.format("Created Reaction: %s", reaction.getUuid()));
        output.output(reaction);
        return true;
    }

    public boolean hasValidationErrors(
            final CommandOutput output,
            final ServiceClient.WithErrorResponse<Reaction> response,
            final String name
    ) throws InputError
    {
        Optional<ReactionValidationError>
                validationError =
                Subscriptions.getValidationError(
                        getClient(),
                        response,
                        name,
                        ReactionValidationError.class
                );
        if (validationError.isPresent()) {
            ReactionValidationError validation = validationError.get();
            output.error("Failed: " + name);
            Subscriptions.outputValidationErrors(
                    output,
                    validation.getMessage(),
                    validation.toMap(),
                    getAppConfig().isAnsiEnabled()
            );
            return true;
        }
        return false;
    }

    @CommandLineInterface(application = "update")
    interface UpdateOpts
            extends ReactionResultOptions, ReactionId, ModifyReactionOptions
    {

    }

    @Command(description = "Update a Reaction. Specify ID, and use other inputs for modification data.")
    public boolean update(UpdateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        final Reaction inputReaction = readReactionInput(options);

        ServiceClient.WithErrorResponse<Reaction> response = apiWithErrorResponse(api -> api.updateReaction(
                project,
                options.getId(),
                inputReaction
        ));

        if (hasValidationErrors(output, response, "Update Reaction [" + options.getId() + "]")) {
            return false;
        }

        Reaction reaction = response.getResponse().body();
        output.info(String.format("Updated Reaction: %s", reaction.getUuid()));
        output.output(reaction);
        return true;
    }

    public Reaction readReactionInput(final ModifyReactionOptions options) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final Reaction inputReaction = objectMapper.readValue(options.getFile(), Reaction.class);

        if (options.isName()) {
            inputReaction.setName(options.getName());
        }
        if (options.isDescription()) {
            inputReaction.setDescription(options.getDescription());
        }
        if (options.isActivation()) {
            inputReaction.setEnabled(options.getActivation().isActive());
        } else if (options.isEnable()) {
            inputReaction.setEnabled(true);
        } else if (options.isDisable()) {
            inputReaction.setEnabled(false);
        }
        return inputReaction;
    }

    @CommandLineInterface(application = "enable")
    interface EnableOpts
            extends ReactionResultOptions, ReactionId
    {

    }

    @Command(description = "Enable a Reaction.")
    public Reaction enable(EnableOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Reaction inputReaction = new Reaction();
        inputReaction.setEnabled(true);
        Reaction reaction = apiCall(api -> api.updateReaction(
                project,
                options.getId(),
                inputReaction
        ));
        output.info(String.format("Enabled Reaction: %s", reaction.getUuid()));
        output.output(reaction);
        return reaction;
    }

    @CommandLineInterface(application = "disable")
    interface DisableOpts
            extends ReactionResultOptions, ReactionId
    {

    }

    @Command(description = "Disable a Reaction.")
    public Reaction disable(DisableOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Reaction inputReaction = new Reaction();
        inputReaction.setEnabled(false);
        Reaction reaction = apiCall(api -> api.updateReaction(
                project,
                options.getId(),
                inputReaction
        ));
        output.info(String.format("Disabled Reaction: %s", reaction.getUuid()));
        output.output(reaction);
        return reaction;
    }

    @Command(description = "Delete a Reaction")
    public void delete(InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Void x = apiCall(api -> api.deleteReaction(
                project,
                options.getId()
        ));
        output.info(String.format("Deleted Reaction: %s", options.getId()));
    }
}
