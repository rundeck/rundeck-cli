package org.rundeck.client.tool.commands.pro;


import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.api.model.pro.Reaction;
import org.rundeck.client.api.model.pro.ReactionEvent;
import org.rundeck.client.api.model.pro.ReactionEventList;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.commands.projects.Configure;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.RdClientConfig;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    @Command
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


    @Command
    public Reaction info(InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Reaction reactionList = apiCall(api -> api.getReactionInfo(
                project,
                options.getId()
        ));
        output.output(reactionList);
        return reactionList;
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
            extends ConfigFileOptions, ToggleOptions
    {

        @Option(shortName = "n", longName = "name", description = "Reaction name")
        String getName();

        boolean isName();

        @Option(shortName = "d", longName = "description", description = "Reaction description (optional)")
        String getDescription();

        boolean isDescription();

    }

    @CommandLineInterface(application = "create")
    interface CreateOpts
            extends ReactionResultOptions, ModifyReactionOptions
    {


    }

    @Command
    public Reaction create(CreateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Reaction inputReaction = new Reaction();
        inputReaction.setEnabled(true);
        if (options.isFile()) {
            throw new InputError("-f/--file required for create");
        }
        configureReaction(options, inputReaction, true);
        if (null == inputReaction.getName()) {
            throw new InputError("-n/--name arg, or \"name\" entry in json is required for create");
        }
        if (null == inputReaction.getSelector()) {
            throw new InputError("\"selector\" entry in json is required for create");
        }
        if (null == inputReaction.getConditions()) {
            throw new InputError("\"conditions\" entry in json is required for create");
        }
        if (null == inputReaction.getHandler()) {
            throw new InputError("\"conditions\" entry in json is required for create");
        }

        Reaction reaction = apiCall(api -> api.createReaction(
                project,
                inputReaction
        ));
        output.info(String.format("Created Reaction: %s", reaction.getUuid()));
        output.output(reaction);
        return reaction;
    }

    private void configureReaction(
            final ModifyReactionOptions options,
            final Reaction inputReaction,
            final boolean require
    )
            throws InputError, IOException
    {
        if (options.isFile()) {
            configureJson(inputReaction, Configure.loadConfigJson(options, require));
        }

        if (options.isName()) {
            inputReaction.setName(options.getName());
        }
        if (options.isDescription()) {
            inputReaction.setDescription(options.getDescription());
        } else if (options.isActivation()) {
            inputReaction.setEnabled(options.getActivation().isActive());
        } else if (options.isEnable()) {
            inputReaction.setEnabled(true);
        } else if (options.isDisable()) {
            inputReaction.setEnabled(false);
        }
    }

    private void configureJson(final Reaction inputReaction, final Map<String, Object> json) {
        if (json.containsKey("selector")) {
            inputReaction.setSelector((Map) json.get("selector"));
        }
        if (json.containsKey("conditions")) {
            inputReaction.setConditions((List) json.get("conditions"));
        }
        if (json.containsKey("handler")) {
            inputReaction.setHandler((Map) json.get("handler"));
        }
        if (json.containsKey("name")) {
            inputReaction.setName((String) json.get("name"));
        }
        if (json.containsKey("description")) {
            inputReaction.setDescription((String) json.get("description"));
        }
        if (json.containsKey("enabled")) {
            inputReaction.setEnabled((boolean) json.get("enabled"));
        }
    }

    @CommandLineInterface(application = "update")
    interface UpdateOpts
            extends ReactionResultOptions, ConfigFileOptions, ReactionId, ModifyReactionOptions
    {

    }

    @Command
    public Reaction update(UpdateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Reaction inputReaction = new Reaction();

        configureReaction(options, inputReaction, false);

        Reaction reaction = apiCall(api -> api.updateReaction(
                project,
                options.getId(),
                inputReaction
        ));
        output.info(String.format("Updated Reaction: %s", reaction.getUuid()));
        output.output(reaction);
        return reaction;
    }

    @CommandLineInterface(application = "enable")
    interface EnableOpts
            extends ReactionResultOptions, ReactionId
    {

    }

    @Command
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

    @Command
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

    @Command
    public void delete(InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Void x = apiCall(api -> api.deleteReaction(
                project,
                options.getId()
        ));
        output.info(String.format("Deleted Reaction: %s", options.getId()));
    }
}
