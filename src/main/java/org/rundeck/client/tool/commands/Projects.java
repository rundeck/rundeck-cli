package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.HasSubCommands;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectItem;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.projects.ACLs;
import org.rundeck.client.tool.commands.projects.Readme;
import org.rundeck.client.tool.commands.projects.SCM;
import org.rundeck.client.tool.options.OptionUtil;
import org.rundeck.client.tool.options.ProjectCreateOptions;
import org.rundeck.client.tool.options.ProjectNameOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by greg on 5/19/16.
 */
@Command(description = "List and manage projects.")
public class Projects extends AppCommand implements HasSubCommands {
    public Projects(final RdApp client) {
        super(client);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(
                new ACLs(this),
                new SCM(this),
                new Readme(this)
        );
    }

    @Command(isDefault = true, description = "List all projects. (no options.)")
    public void list(CommandOutput output) throws IOException, InputError {
        List<ProjectItem> body = apiCall(RundeckApi::listProjects);
        output.info(String.format("%d Projects:%n", body.size()));
        output.output(body.stream().map(ProjectItem::toBasicString).collect(Collectors.toList()));
    }

    @CommandLineInterface(application = "delete") interface ProjectDelete extends ProjectNameOptions {
        @Option(longName = "confirm", shortName = "y", description = "Force confirmation of delete request.")
        boolean isConfirm();

    }

    @Command(description = "Delete a project")
    public boolean delete(ProjectDelete options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        if (!options.isConfirm()) {
            //request confirmation
            String s = System.console().readLine("Really delete project %s? (y/N) ", project);

            if (!"y".equals(s)) {
                output.warning(String.format("Not deleting project %s.", project));
                return false;
            }
        }
        apiCall(api -> api.deleteProject(project));
        output.info(String.format("Project was deleted: %s%n", project));
        return true;
    }

    @CommandLineInterface(application = "create") interface Create extends ProjectCreateOptions {

    }

    @Command(description = "Create a project.")
    public boolean create(Create options, CommandOutput output) throws IOException, InputError {
        Map<String, String> config = OptionUtil.parseKeyValueMap(options.config());
        ProjectItem project = new ProjectItem();
        project.setName(projectOrEnv(options));
        project.setConfig(config);

        ProjectItem body = apiCall(api -> api.createProject(project));
        output.info(String.format("Created project: \n\t%s%n", body.toBasicString()));
        return true;
    }

}
