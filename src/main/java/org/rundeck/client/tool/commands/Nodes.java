package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectNode;
import org.rundeck.client.tool.options.NodeOutputFormatOption;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Format;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.rundeck.client.tool.options.OptionUtil.projectOrEnv;

/**
 * @author greg
 * @since 11/22/16
 */
@Command(description = "List and manage node resources.")
public class Nodes extends ApiCommand {
    public Nodes(final HasClient builder) {
        super(builder);
    }

    @CommandLineInterface(application = "list") interface ListOptions extends ProjectNameOptions,
            VerboseOption,
            NodeOutputFormatOption
    {
        @Option(shortName = "f", longName = "filter", description = "Node filter")
        String getFilter();

        boolean isFilter();

        @Unparsed(name = "NODE FILTER", description = "Node filter")
        List<String> getFilterTokens();

        boolean isFilterTokens();
    }

    String filterString(ListOptions options) {
        if (options.isFilter()) {
            return options.getFilter();
        } else if (options.isFilterTokens()) {
            return String.join(" ", options.getFilterTokens());
        }
        return null;
    }

    @Command(description = "List all nodes for a project.  You can use the -f/--filter to specify a node filter, or " +
                           "simply add the filter on the end of the command")
    public void list(ListOptions options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Map<String, ProjectNode> body = getClient().checkError(getClient().getService()
                                                                          .listNodes(
                                                                                  project,
                                                                                  filterString(options)
                                                                          ));
        if (!options.isOutputFormat()) {
            output.info(String.format("%d Nodes%s in project %s:%n", body.size(),
                                      options.isFilter() ? " matching filter" : "",
                                      project
            ));
        }
        Function<ProjectNode, ?> field;
        if (options.isOutputFormat()) {
            field = Format.formatter(options.getOutputFormat(), ProjectNode::getAttributes, "%", "");
        } else if (options.isVerbose()) {
            field = ProjectNode::getAttributes;
        } else {
            field = ProjectNode::getName;
        }
        output.output(body.values().stream().map(field).collect(Collectors.toList()));
    }
}
