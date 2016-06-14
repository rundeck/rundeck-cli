package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectItem;
import org.rundeck.client.tool.commands.projects.ACLs;
import org.rundeck.client.tool.options.ProjectCreateOptions;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import org.rundeck.util.toolbelt.*;
import org.rundeck.util.toolbelt.input.jewelcli.JewelInput;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by greg on 5/19/16.
 */
@Command(description = "List and manage projects.")
public class Projects extends ApiCommand implements HasSubCommands{
    public Projects(final Client<RundeckApi> client) {
        super(client);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(
                new ACLs(client)
        );
    }

    @Command(isDefault = true, description = "List all projects. (no options.)")
    public void list(CommandOutput output) throws IOException {
        List<ProjectItem> body = client.checkError(client.getService().listProjects());
        output.output(String.format("%d Projects:%n", body.size()));
        output.output(body.stream().map(ProjectItem::toBasicString).collect(Collectors.toList()));
    }

    @CommandLineInterface(application = "delete") interface ProjectDelete extends ProjectNameOptions {

    }

    @Command(description = "Delete a project")
    public void delete(ProjectDelete options, CommandOutput output) throws IOException {
        client.checkError(client.getService().deleteProject(options.getProject()));
        output.output(String.format("Project was deleted: %s%n", options.getProject()));
    }

    @CommandLineInterface(application = "create") interface Create extends ProjectCreateOptions {

    }

    @Command(description = "Create a project.")
    public void create(Create options, CommandOutput output) throws IOException {
        Map<String, String> config = new HashMap<>();
        if (options.config().size() > 0) {
            for (String s : options.config()) {
                if (!s.startsWith("--")) {
                    throw new IllegalArgumentException("Expected --key=value, but saw: " + s);
                }
                s = s.substring(2);
                String[] arr = s.split("=", 2);
                if (arr.length != 2) {
                    throw new IllegalArgumentException("Expected --key=value, but saw: " + s);
                }
                config.put(arr[0], arr[1]);
            }
        }
        ProjectItem project = new ProjectItem();
        project.setName(options.getProject());
        project.setConfig(config);

        ProjectItem body = client.checkError(client.getService().createProject(project));
        output.output(String.format("Created project: \n\t%s%n", body.toBasicString()));
    }

}
