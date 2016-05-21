package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.AdhocResponse;
import org.rundeck.client.api.model.ExecOutput;
import org.rundeck.client.tool.options.AdhocBaseOptions;
import retrofit2.Call;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
public class Adhoc {

    public static void main(String[] args) throws IOException {
        String baseUrl = App.requireEnv("RUNDECK_URL", "Please specify the Rundeck URL");
        String token = App.requireEnv("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
        RundeckApi client = Rundeck.client(baseUrl, token, System.getenv("DEBUG") != null);
        boolean success = dispatch(args, client);
        if (!success) {
            System.exit(2);
        }
    }

    private static boolean dispatch(final String[] args, final RundeckApi client) throws IOException {
        AdhocBaseOptions options = CliFactory.parseArguments(AdhocBaseOptions.class, args);

        Call<AdhocResponse> adhocResponseCall = null;

        if (options.isScriptFile()) {
            File input = options.getScriptFile();
            if (!input.canRead() || !input.isFile()) {
                throw new IllegalArgumentException(String.format("File is not readable or does not exist: %s", input));
            }
            //TODO: read stdin

            RequestBody scriptFileBody = RequestBody.create(
                    MediaType.parse("application/octet-stream"),
                    input
            );

            adhocResponseCall = client.runScript(
                    options.getProject(),
                    MultipartBody.Part.createFormData("scriptFile",input.getName(),scriptFileBody),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    joinString(options.getCommandString()),
                    null,
                    false,
                    null,
                    options.getFilter()
            );
        } else if (options.isUrl()) {

            //TODO: url
        } else if (options.getCommandString() != null && options.getCommandString().size() > 0) {
            //command
            adhocResponseCall = client.runCommand(
                    options.getProject(),
                    joinString(options.getCommandString()),
                    options.getThreadcount(),
                    options.isKeepgoing(),
                    options.getFilter()
            );
        } else {
            throw new IllegalArgumentException("-s, -u, or -- command string, was expected");
        }

        AdhocResponse adhocResponse = App.checkError(adhocResponseCall);
        System.out.println(adhocResponse.message);
        System.out.println("Started execution " + adhocResponse.execution.toBasicString());
        if (!options.isFollow()) {
            return true;
        }
        Call<ExecOutput> execOutputCall = Executions.startFollowOutput(
                client,
                500,
                true,
                adhocResponse.execution.getId(),
                0
        );
        return Executions.followOutput(
                client,
                execOutputCall,
                options.isProgress(),
                options.isQuiet(),
                adhocResponse.execution.getId(),
                500
        );
    }

    private static String joinString(final List<String> commandString) {
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
