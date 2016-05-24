package org.rundeck.client.tool.commands;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.AdhocResponse;
import org.rundeck.client.belt.Command;
import org.rundeck.client.belt.CommandOutput;
import org.rundeck.client.belt.CommandRunFailure;
import org.rundeck.client.tool.App;
import org.rundeck.client.tool.options.AdhocBaseOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Util;
import retrofit2.Call;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Created by greg on 5/20/16.
 */

@Command(description = "Dispatch and adhoc COMMAND to matching nodes.")
public class Adhoc extends ApiCommand {
    static final String COMMAND = "adhoc";

    public Adhoc(final Client<RundeckApi> client) {
        super(client);
    }

    @CommandLineInterface(application = COMMAND) interface Dispatch extends AdhocBaseOptions {

    }

    @Command(isSolo = true, isDefault = true)
    public boolean dispatch(Dispatch options, CommandOutput output) throws IOException {
        Call<AdhocResponse> adhocResponseCall = null;

        if (options.isScriptFile() || options.isStdin()) {
            RequestBody scriptFileBody;
            String filename;
            if (options.isScriptFile()) {
                File input = options.getScriptFile();
                if (!input.canRead() || !input.isFile()) {
                    throw new IllegalArgumentException(String.format(
                            "File is not readable or does not exist: %s",
                            input
                    ));
                }

                scriptFileBody = RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        input
                );
                filename = input.getName();
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                long bytes = Util.copyStream(System.in, byteArrayOutputStream);

                scriptFileBody = RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        byteArrayOutputStream.toByteArray()

                );
                filename = "script.sh";
            }

            adhocResponseCall = client.getService().runScript(
                    options.getProject(),
                    MultipartBody.Part.createFormData("scriptFile", filename, scriptFileBody),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    joinString(options.getCommandString()),
                    null,
                    false,
                    null,
                    options.getFilter()
            );
        } else if (options.isUrl()) {
            adhocResponseCall = client.getService().runUrl(
                    options.getProject(),
                    options.getUrl(),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    joinString(options.getCommandString()),
                    null,
                    false,
                    null,
                    options.getFilter()
            );
        } else if (options.getCommandString() != null && options.getCommandString().size() > 0) {
            //command
            adhocResponseCall = client.getService().runCommand(
                    options.getProject(),
                    joinString(options.getCommandString()),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    options.getFilter()
            );
        } else {
            throw new IllegalArgumentException("-s, -u, or -- command string, was expected");
        }

        AdhocResponse adhocResponse = client.checkError(adhocResponseCall);
        output.output(adhocResponse.message);
        output.output("Started execution " + adhocResponse.execution.toBasicString());
        return Executions.maybeFollow(client, options, adhocResponse.execution.getId(), output);
    }

    static String joinString(final List<String> commandString) {
        if (null == commandString) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : commandString) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (s.contains(" ")) {
                sb.append("\"" + s.replaceAll("\\\\", "\\\\").replaceAll("\\\"", "\\\\\"") + "\"");
            } else {
                sb.append(s);
            }
        }
        return sb.toString();
    }

}
