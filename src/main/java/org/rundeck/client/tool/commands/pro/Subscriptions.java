package org.rundeck.client.tool.commands.pro;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.api.model.Subscription;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.commands.projects.Configure;
import org.rundeck.client.tool.options.BaseOptions;
import org.rundeck.client.tool.options.ConfigFileOptions;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.client.util.Format;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    @Command
    public List<Subscription> list(ListOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        List<Subscription> result = apiCall(api -> api.listSubscriptions(
                project
        ));
        if (!options.isOutputFormat()) {
            output.info(String.format("%d Subscriptions in project %s%n", result.size(), project));
        }
        outputList(options, output, result);
        return result;
    }


    private void outputList(
            final ResultOptions options,
            final CommandOutput output,
            final List<Subscription> list
    )
    {
        final Function<Subscription, ?> outformat;
        if (options.isVerbose()) {
            output.output(list.stream().map(Subscription::asMap).collect(Collectors.toList()));
            return;
        }
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), Subscription::asMap, "%", "");
        } else {
            outformat = Subscription::toBasicString;
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

    @Command
    public Subscription info(InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Subscription reactionList = apiCall(api -> api.getSubscriptionInfo(
                project,
                options.getId()
        ));
        output.output(reactionList);
        return reactionList;
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

    @Command
    public Subscription create(Subscriptions.CreateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Subscription inputSubscription = configureSubscription(new Subscription(), options, project);
        Subscription result = apiCall(api -> api.createSubscription(
                project,
                inputSubscription
        ));
        output.output(result);
        return result;
    }

    private Subscription configureSubscription(
            Subscription inputSubscription,
            final ModifyOptions options,
            final String project
    )
            throws InputError, IOException
    {

        inputSubscription.setProject(project);
        Map<String, Object> json = Configure.loadConfigJson(options, true);
        if (options.isType()) {
            inputSubscription.setType(options.getType());
        } else if (json.containsKey("type")) {
            inputSubscription.setType((String) json.get("type"));
        } else {
            throw new InputError("Expected -t/--type arg, or json file to have a \"type\" value");
        }
        if (!json.containsKey("config")) {
            throw new InputError("Expected json file to have a \"config\" map");
        }
        inputSubscription.setConfig((Map) json.get("config"));
        return inputSubscription;
    }

    @CommandLineInterface(application = "update")
    interface UpdateOpts
            extends ResultOptions, ModifyOptions, SubscriptionId
    {
    }

    @Command
    public Subscription update(UpdateOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Subscription input = configureSubscription(
                new Subscription(),
                options,
                project
        );
        Subscription result = apiCall(api -> api.updateSubscription(project, options.getId(), input));
        output.info(String.format("Updated Subscription: %s", options.getId()));
        output.output(result);
        return result;
    }

    @CommandLineInterface(application = "enable")
    interface EnableOpts
            extends ResultOptions, SubscriptionId
    {

    }

    @Command
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

    @Command
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


    @Command
    public void delete(Subscriptions.InfoOpts options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Void x = apiCall(api -> api.deleteSubscription(
                project,
                options.getId()
        ));
        output.info(String.format("Deleted subscription: %s", options.getId()));
    }
}
