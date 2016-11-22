package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectNode;
import org.rundeck.client.tool.options.NodeOutputFormatOption;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Format;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author greg
 * @since 11/22/16
 */
@Command(description = "List and manage node resources.")
public class Nodes extends ApiCommand {
    public Nodes(final Supplier<Client<RundeckApi>> builder) {
        super(builder);
    }

    @CommandLineInterface(application = "list") interface ListOptions extends ProjectNameOptions,
            VerboseOption,
            NodeOutputFormatOption
    {

    }

    @Command(description = "List all nodes for a project")
    public void list(ListOptions options, CommandOutput output) throws IOException {
        Map<String, ProjectNode> body = getClient().checkError(getClient().getService()
                                                                          .listNodes(options.getProject()));
        if (!options.isOutputFormat()) {
            output.info(String.format("%d Nodes in project %s:%n", body.size(), options.getProject()));
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
