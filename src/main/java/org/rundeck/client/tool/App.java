package org.rundeck.client.tool;

import com.simplifyops.toolbelt.CommandRunFailure;
import com.simplifyops.toolbelt.Tool;
import com.simplifyops.toolbelt.ToolBelt;
import com.simplifyops.toolbelt.format.json.jackson.JsonFormatter;
import com.simplifyops.toolbelt.format.yaml.snakeyaml.YamlFormatter;
import com.simplifyops.toolbelt.input.jewelcli.JewelInput;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.DateInfo;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.ScheduledJobItem;
import org.rundeck.client.tool.commands.*;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Env;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;


/**
 * Entrypoint for commandline
 */
public class App {

    public static void main(String[] args) throws IOException, CommandRunFailure {
        setup(tool("rd"), args).runMain(args, true);
    }

    public static Tool setup(final ToolBelt belt, final String[] args) {
        setupFormat(belt);
        return belt.buckle();
    }

    private static void setupFormat(final ToolBelt belt) {
        if ("yaml".equalsIgnoreCase(Env.getString("RD_FORMAT", null))) {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(
                    "BLOCK".equalsIgnoreCase(Env.getString("RD_YAML_FLOW", "BLOCK")) ?
                    DumperOptions.FlowStyle.BLOCK :
                    DumperOptions.FlowStyle.FLOW
            );
            dumperOptions.setPrettyFlow(Env.getBool("RD_YAML_PRETTY", true));
            Representer representer = new Representer();
            representer.addClassTag(JobItem.class, Tag.MAP);
            representer.addClassTag(ScheduledJobItem.class, Tag.MAP);
            representer.addClassTag(DateInfo.class, Tag.MAP);
            representer.addClassTag(Execution.class, Tag.MAP);
            belt.formatter(new YamlFormatter(representer, dumperOptions));
        } else if ("json".equalsIgnoreCase(Env.getString("RD_FORMAT", null))) {
            belt.formatter(new JsonFormatter());
        }
    }

    public static ToolBelt tool(final String name) {
        Client<RundeckApi> client = createClient();
        return ToolBelt.belt(name)
                       .defaultHelpCommands()
                       .ansiColorOutput("1".equals(System.getenv("RD_COLOR")) ||
                                                   System.getenv("TERM") != null &&
                                                   System.getenv("TERM").contains("color"))
                       .add(
                               new Adhoc(client),
                               new Jobs(client),
                               new Projects(client),
                               new Executions(client),
                               new Run(client),
                               new Keys(client),
                               new RDSystem(client),
                               new Scheduler(client),
                               new Tokens(client)
                       )
                       .commandInput(new JewelInput());
    }



    public static Client<RundeckApi> createClient() {
        String baseUrl = Env.require(
                "RUNDECK_URL",
                "Please specify the Rundeck base URL, e.g. http://host:port or http://host:port/api/14"
        );
        if (System.getenv("RUNDECK_TOKEN") == null
            && System.getenv("RUNDECK_USER") == null
            && System.getenv("RUNDECK_PASSWORD") == null) {

            throw new IllegalArgumentException(
                    "Environment variable RUNDECK_TOKEN or RUNDECK_USER and RUNDECK_PASSWORD are required");
        }
        int debuglevel = Env.getInt("DEBUG", 0);
        Long httpTimeout = Env.getLong("RD_HTTP_TIMEOUT", null);
        Boolean retryConnect = Env.getBool("RD_CONNECT_RETRY", true);

        if (null != System.getenv("RUNDECK_TOKEN")) {
            String token = Env.require("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
            return Rundeck.client(baseUrl, token, debuglevel, httpTimeout, retryConnect);
        } else {

            String user = Env.require("RUNDECK_USER", "Please specify the Rundeck username");
            String pass = Env.require("RUNDECK_PASSWORD", "Please specify the Rundeck password");
            return Rundeck.client(baseUrl, user, pass, debuglevel, httpTimeout, retryConnect);
        }
    }

}
