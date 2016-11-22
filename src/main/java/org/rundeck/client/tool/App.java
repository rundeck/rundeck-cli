package org.rundeck.client.tool;

import com.simplifyops.toolbelt.*;
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
        tool("rd").runMain(args, true);
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
            belt.channels().infoEnabled(false);
        } else if ("json".equalsIgnoreCase(Env.getString("RD_FORMAT", null))) {
            belt.formatter(new JsonFormatter());
            belt.channels().infoEnabled(false);
        } else {
            NiceFormatter formatter = new NiceFormatter(null);
            formatter.setCollectionIndicator("");
            belt.formatter(formatter);
            belt.channels().info(new FormattedOutput(
                    belt.defaultOutput(),
                    new PrefixFormatter("# ", belt.defaultBaseFormatter())
            ));
        }
    }

    public static Tool tool(final String name) {
        ToolBelt belt = ToolBelt.belt(name)
                                .defaultHelpCommands()
                                .ansiColorOutput(isAnsiEnabled())
                                .add(
                                        new Adhoc(App::createClient),
                                        new Jobs(App::createClient),
                                        new Projects(App::createClient),
                                        new Executions(App::createClient),
                                        new Run(App::createClient),
                                        new Keys(App::createClient),
                                        new RDSystem(App::createClient),
                                        new Scheduler(App::createClient),
                                        new Tokens(App::createClient)
                                )
                                .commandInput(new JewelInput());
        setupFormat(belt);
        return belt.buckle();
    }

    private static boolean isAnsiEnabled() {
        return "1".equals(System.getenv("RD_COLOR")) ||
               System.getenv("TERM") != null &&
               System.getenv("TERM").contains("color");
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
