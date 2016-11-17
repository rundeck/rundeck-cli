package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.rundeck.client.api.ReadmeFile;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectReadme;
import org.rundeck.client.tool.commands.ApiCommand;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;

import java.io.File;
import java.io.IOException;

/**
 * Created by greg on 9/15/16.
 */

@Command(description = "Manage Project readme.md/motd.md")
public class Readme extends ApiCommand {
    public Readme(final Client<RundeckApi> client) {
        super(client);
    }

    public interface GetOptions extends ProjectNameOptions {
        /**
         * @return
         */
        @Option(shortName = "m",
                longName = "motd",
                description = "Choose the 'motd.md' file. If unset, choose 'readme.md'.")
        public boolean isMotd();

    }

    public ReadmeFile getReadmeFile(GetOptions options) {
        return options.isMotd() ? ReadmeFile.MOTD : ReadmeFile.README;
    }

    @Command(description = "get project readme/motd file")
    public void get(GetOptions options, CommandOutput output) throws IOException {
        ProjectReadme readme = client.checkError(client.getService()
                                                       .getReadme(
                                                               options.getProject(),
                                                               getReadmeFile(options)
                                                       ));
        output.output(readme.getContents());
    }


    public interface SetOptions extends GetOptions {
        /**
         * @return
         */
        @Option(shortName = "f", longName = "file", description = "Path to a file to read for readme/motd contents.")
        public File getFile();

        public boolean isFile();

        /**
         * @return
         */
        @Option(shortName = "t", longName = "text", description = "Text to use for readme/motd contents.")
        public String getText();

        public boolean isText();
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
        ProjectReadme readme = client.checkError(client.getService()
                                                       .putReadme(
                                                               options.getProject(),
                                                               getReadmeFile(options),
                                                               requestBody
                                                       ));
        output.output(readme.getContents());
    }


    @Command(description = "delete project readme/motd file")
    public void delete(GetOptions options, CommandOutput output) throws IOException {
        Void readme = client.checkError(client.getService()
                                              .deleteReadme(
                                                      options.getProject(),
                                                      getReadmeFile(options)
                                              ));

    }
}
