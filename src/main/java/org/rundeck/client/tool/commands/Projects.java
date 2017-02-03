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
import org.rundeck.client.tool.commands.projects.Configure;
import org.rundeck.client.tool.commands.projects.Readme;
import org.rundeck.client.tool.commands.projects.SCM;
import org.rundeck.client.tool.options.*;
import org.rundeck.client.util.Format;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
                new Readme(this),
                new Configure(this)
        );
    }

    interface ProjectResultOptions extends ProjectListFormatOptions, VerboseOption {

    }

    @CommandLineInterface(application = "list") interface ProjectListOpts extends ProjectResultOptions {

    }

    @Command(isDefault = true, description = "List all projects.")
    public void list(ProjectListOpts opts, CommandOutput output) throws IOException, InputError {
        List<ProjectItem> body = apiCall(RundeckApi::listProjects);
        if (!opts.isOutputFormat()) {
            output.info(String.format("%d Projects:%n", body.size()));
        }

        outputProjectList(opts, output, body, ProjectItem::getName, ProjectItem::toMap);
    }

    @CommandLineInterface(application = "info") interface ProjectInfoOpts extends ProjectResultOptions {

        @Option(shortName = "p", longName = "project", description = "Project name")
        String getProject();

    }

    @Command(isDefault = true,
             description = "Get info about a project. Use -v/--verbose to output all available config data, or use " +
                           "-%/--outformat for selective data.")
    public void info(ProjectInfoOpts opts, CommandOutput output) throws IOException, InputError {
        ProjectItem body = apiCall(api -> api.getProjectInfo(opts.getProject()));

        outputProjectList(opts, output, Collections.singletonList(body), ProjectItem::toBasicMap, ProjectItem::toMap);
    }

    private void outputProjectList(
            final ProjectResultOptions options,
            final CommandOutput output,
            final List<ProjectItem> body,
            final Function<ProjectItem, Object> basicOutput,
            final Function<ProjectItem, Map<Object, Object>> verboseOutput
    )
    {
        final Function<ProjectItem, ?> outformat;
        if (options.isVerbose()) {
            output.output(body.stream().map(verboseOutput).collect(Collectors.toList()));
            return;
        }
        if (options.isOutputFormat()) {
            outformat = Format.formatter(options.getOutputFormat(), ProjectItem::toMap, "%", "");
        } else {
            outformat = basicOutput;
        }

        output.output(body.stream().map(outformat).collect(Collectors.toList()));
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
