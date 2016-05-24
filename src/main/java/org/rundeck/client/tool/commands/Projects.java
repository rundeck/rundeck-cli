package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CliFactory;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectItem;
import org.rundeck.client.belt.Command;
import org.rundeck.client.belt.CommandRunFailure;
import org.rundeck.client.tool.App;
import org.rundeck.client.tool.options.ProjectCreateOptions;
import org.rundeck.client.tool.options.ProjectOptions;
import org.rundeck.client.util.Client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by greg on 5/19/16.
 */
@Command
public class Projects extends ApiCommand {
    public Projects(final Client<RundeckApi> client) {
        super(client);
    }

    public static void main(String[] args) throws IOException, CommandRunFailure {
        App.tool(new Projects(App.createClient())).run(args);
    }

    @Command(isDefault = true)
    public void list() throws IOException {
        List<ProjectItem> body = client.checkError(client.getService().listProjects());
        System.out.printf("%d Projects:%n", body.size());
        for (ProjectItem proj : body) {
            System.out.println("* " + proj.toBasicString());
        }

    }

    @Command
    public void delete(ProjectOptions projectOptions) throws IOException {
        client.checkError(client.getService().deleteProject(projectOptions.getProject()));
        System.out.printf("Project was deleted: %s%n", projectOptions.getProject());
    }

    @Command
    public void create(ProjectCreateOptions options) throws IOException {
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
        System.out.printf("Created project: \n\t%s%n", body.toBasicString());
    }
}
