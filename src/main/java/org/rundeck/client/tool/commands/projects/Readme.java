package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.rundeck.client.api.ReadmeFile;
import org.rundeck.client.api.model.ProjectReadme;
import org.rundeck.client.tool.commands.ApiCommand;
import org.rundeck.client.tool.commands.HasClient;
import org.rundeck.client.tool.options.ProjectNameOptions;

import java.io.File;
import java.io.IOException;


/**
 * Created by greg on 9/15/16.
 */

@Command(description = "Manage Project readme.md/motd.md")
public class Readme extends ApiCommand {
    public Readme(final HasClient client) {
        super(client);
    }

    public interface GetOptions extends ProjectNameOptions {
        /**
         * @return
         */
        @Option(shortName = "m",
                longName = "motd",
                description = "Choose the 'motd.md' file. If unset, choose 'readme.md'.")
        boolean isMotd();

    }

    public ReadmeFile getReadmeFile(GetOptions options) {
        return options.isMotd() ? ReadmeFile.MOTD : ReadmeFile.README;
    }

    @Command(description = "get project readme/motd file")
    public void get(GetOptions options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        ProjectReadme readme = apiCall(api -> api.getReadme(project, getReadmeFile(options)));
        output.output(readme.getContents());
    }


    public interface SetOptions extends GetOptions {
        /**
         * @return
         */
        @Option(shortName = "f", longName = "file", description = "Path to a file to read for readme/motd contents.")
        File getFile();

        boolean isFile();

        /**
         * @return
         */
        @Option(shortName = "t", longName = "text", description = "Text to use for readme/motd contents.")
        String getText();

        boolean isText();
    }


    @Command(description = "set project readme/motd file")
    public void put(SetOptions options, CommandOutput output) throws IOException, InputError {
        if (!options.isText() && !options.isFile()) {
            throw new InputError("-f/--file or -t/--text is required");
        }
        RequestBody requestBody;
        if (options.isFile()) {
            requestBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    options.getFile()
            );
        } else {
            requestBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    options.getText()
            );
        }
        String project = projectOrEnv(options);
        ProjectReadme readme = apiCall(api -> api.putReadme(project, getReadmeFile(options), requestBody));
        output.output(readme.getContents());
    }


    @Command(description = "delete project readme/motd file")
    public void delete(GetOptions options, CommandOutput output) throws IOException, InputError {
        String project = projectOrEnv(options);
        Void readme = apiCall(api -> api.deleteReadme(project, getReadmeFile(options)));
        output.info(String.format("Deleted %s for project %s", getReadmeFile(options), project));
    }
}
