package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.HasSubCommands;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectItem;
import org.rundeck.client.tool.commands.projects.ACLs;
import org.rundeck.client.tool.commands.projects.Readme;
import org.rundeck.client.tool.commands.projects.SCM;
import org.rundeck.client.tool.options.ProjectCreateOptions;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by greg on 5/19/16.
 */
@Command(description = "List and manage projects.")
public class Projects extends ApiCommand implements HasSubCommands {
    public Projects(final HasClient client) {
        super(client);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(
                new ACLs(this::getClient),
                new SCM(this::getClient),
                new Readme(this::getClient)
        );
    }

    @Command(isDefault = true, description = "List all projects. (no options.)")
    public void list(CommandOutput output) throws IOException, InputError {
        List<ProjectItem> body = getClient().checkError(getClient().getService().listProjects());
        output.info(String.format("%d Projects:%n", body.size()));
        output.output(body.stream().map(ProjectItem::toBasicString).collect(Collectors.toList()));
    }

    @CommandLineInterface(application = "delete") interface ProjectDelete extends ProjectNameOptions {
        @Option(longName = "confirm", shortName = "y", description = "Force confirmation of delete request.")
        boolean isConfirm();

    }

    @Command(description = "Delete a project")
    public boolean delete(ProjectDelete options, CommandOutput output) throws IOException, InputError {
        if (!options.isConfirm()) {
            //request confirmation
            String s = System.console().readLine("Really delete project %s? (y/N) ", options.getProject());

            if (!"y".equals(s)) {
                output.warning(String.format("Not deleting project %s.", options.getProject()));
                return false;
            }
        }
        getClient().checkError(getClient().getService().deleteProject(options.getProject()));
        output.info(String.format("Project was deleted: %s%n", options.getProject()));
        return true;
    }

    @CommandLineInterface(application = "create") interface Create extends ProjectCreateOptions {

    }

    @Command(description = "Create a project.")
    public boolean create(Create options, CommandOutput output) throws IOException, InputError {
        Map<String, String> config = new HashMap<>();
        if (options.config().size() > 0) {
            for (String s : options.config()) {
                if (!s.startsWith("--")) {
                    throw new InputError("Expected --key=value, but saw: " + s);
                }
                s = s.substring(2);
                String[] arr = s.split("=", 2);
                if (arr.length != 2) {
                    throw new InputError("Expected --key=value, but saw: " + s);
                }
                config.put(arr[0], arr[1]);
            }
        }
        ProjectItem project = new ProjectItem();
        project.setName(options.getProject());
        project.setConfig(config);

        ProjectItem body = getClient().checkError(getClient().getService().createProject(project));
        output.info(String.format("Created project: \n\t%s%n", body.toBasicString()));
        return true;
    }

}
