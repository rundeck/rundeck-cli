package org.rundeck.client.tool;

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
        tool("rd").runMain(args, true);
    }

    public static Tool tool(final String name) {
        Client<RundeckApi> client = createClient();
        return ToolBelt.belt(name)
                       .defaultHelpCommands()
                       .ansiColorOutput("1".equals(System.getenv("RD_COLOR")))
                       .add(
                               new Adhoc(client),
                               new Jobs(client),
                               new Projects(client),
                               new Executions(client),
                               new Run(client),
                               new Keys(client),
                               new RDSystem(client),
                               new Scheduler(client)
                       )
                       .commandInput(new JewelInput())
                       .buckle();
    }


    public static Client<RundeckApi> createClient() {
        String baseUrl = requireEnv(
                "RUNDECK_URL",
                "Please specify the Rundeck base URL, e.g. http://host:port or http://host:port/api/14"
        );
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
