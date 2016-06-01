package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.commands.*;
import org.rundeck.client.util.Client;
import org.rundeck.util.toolbelt.CommandRunFailure;
import org.rundeck.util.toolbelt.Tool;
import org.rundeck.util.toolbelt.ToolBelt;
import org.rundeck.util.toolbelt.input.jewelcli.JewelInput;

import java.io.IOException;

/**
 * Entrypoint for commandline
 */
public class App {

    public static void main(String[] args) throws IOException, CommandRunFailure {
        tool().runMain(args, true);
    }

    public static Tool tool() {
        Client<RundeckApi> client = createClient();
        return ToolBelt.belt()
                       .defaultHelpCommands()
                       .systemOutput()
                       .add(
                                  new Adhoc(client),
                                  new Jobs(client),
                                  new Projects(client),
                                  new Executions(client),
                                  new Run(client)
                          )
                       .commandInput(new JewelInput())
                       .buckle();
    }


    public static Client<RundeckApi> createClient() {
        String baseUrl = requireEnv("RUNDECK_URL", "Please specify the Rundeck URL");
        if (System.getenv("RUNDECK_TOKEN") == null
            && System.getenv("RUNDECK_USER") == null
            && System.getenv("RUNDECK_PASSWORD") == null) {

            throw new IllegalArgumentException(
                    "Environment variable RUNDECK_TOKEN or RUNDECK_USER and RUNDECK_PASSWORD are required");
        }
        if (null != System.getenv("RUNDECK_TOKEN")) {
            String token = requireEnv("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
            return Rundeck.client(baseUrl, token, System.getenv("DEBUG") != null);
        } else {

            String user = requireEnv("RUNDECK_USER", "Please specify the Rundeck username");
            String pass = requireEnv("RUNDECK_PASSWORD", "Please specify the Rundeck password");
            return Rundeck.client(baseUrl, user, pass, System.getenv("DEBUG") != null);
        }
    }


    public static String requireEnv(final String name, final String description) {
        String value = System.getenv(name);
        if (null == value) {
            throw new IllegalArgumentException(String.format(
                    "Environment variable %s is required: %s",
                    name,
                    description
            ));
        }
        return value;
    }

}
