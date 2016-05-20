package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectItem;
import org.rundeck.client.tool.options.ProjectCreateOptions;
import org.rundeck.client.tool.options.ProjectOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by greg on 5/19/16.
 */
public class Projects {

    public static void main(String[] args) throws IOException {
        String baseUrl = App.requireEnv("RUNDECK_URL", "Please specify the Rundeck URL");
        String token = App.requireEnv("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
        RundeckApi client = Rundeck.client(baseUrl, token, System.getenv("DEBUG")!=null);
        if ("list".equals(args[0])) {
            list(App.tail(args), client);
        } else if ("create".equals(args[0])) {
            create(App.tail(args), client);
        } else if ("delete".equals(args[0])) {
            delete(App.tail(args), client);
        }else{
            throw new IllegalArgumentException(String.format("Unrecognized action: %s", args[0]));
        }

    }

    private static void list(final String[] args, final RundeckApi client) throws IOException {
        ProjectOptions jobListOptions = CliFactory.parseArguments(ProjectOptions.class, args);

        List<ProjectItem> body = App.checkError(client.listProjects());
        System.out.printf("%d Projects:%n", body.size());
        for (ProjectItem proj : body) {
            System.out.println("* " + proj.toBasicString());
        }

    }

    private static void delete(final String[] args, final RundeckApi client) throws IOException {
        ProjectOptions projectOptions = CliFactory.parseArguments(ProjectOptions.class, args);
        if (!projectOptions.isProject()) {
            throw new IllegalArgumentException("-p is required");
        }

        Void body = App.checkError(client.deleteProject(projectOptions.getProject()));
        System.out.printf("Project was deleted: %s%n", projectOptions.getProject());
    }

    private static void create(final String[] args, final RundeckApi client) throws IOException {
        ProjectCreateOptions createOptions = CliFactory.parseArguments(ProjectCreateOptions.class, args);
        if (!createOptions.isProject()) {
            throw new IllegalArgumentException("-p is required");
        }
        Map<String, String> config = new HashMap<>();
        if (createOptions.config().size() > 0) {
            for (String s : createOptions.config()) {
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
        project.setName(createOptions.getProject());
        project.setConfig(config);

        ProjectItem body = App.checkError(client.createProject(project));
        System.out.printf("Created project: \n\t%s%n", body.toBasicString());
    }
}
