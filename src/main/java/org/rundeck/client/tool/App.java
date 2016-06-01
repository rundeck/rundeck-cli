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
 * Created by greg on 3/28/16.
 */
public class App {
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_JSON = "application/json";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse(APPLICATION_JSON);
    public static final MediaType MEDIA_TYPE_XML = MediaType.parse(APPLICATION_XML);
    public static final String APPLICATION_YAML = "application/yaml";
    public static final MediaType MEDIA_TYPE_YAML = MediaType.parse(APPLICATION_YAML);
    public static final MediaType MEDIA_TYPE_TEXT_YAML = MediaType.parse("text/yaml");
    public static final MediaType MEDIA_TYPE_TEXT_XML = MediaType.parse("text/xml");

    public static void main(String[] args) throws IOException, CommandRunFailure {
        tool().runMain(args, true);
    }

    public static void main(Object command, String[] args) throws CommandRunFailure {
        tool(command).runMain(args, true);
    }

    public static Tool tool(Object command) {
        return ToolBelt.belt().add(command).commandInput(new JewelInput()).buckle();
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

    public static boolean hasAnyMediaType(final ResponseBody body, final MediaType... parse) {
        MediaType mediaType1 = body.contentType();
        for (MediaType mediaType : parse) {
            if (mediaType1.type().equals(mediaType.type()) && mediaType1.subtype().equals(mediaType.subtype())) {
                return true;
            }
        }
        return false;
    }


    public static <T> T parse(Class<T> clazz, String[] args) {
        T options = CliFactory.parseArguments(clazz, args);
        return options;
    }

    public <T> T parseArgs(final String[] args, final Class<? extends T> clazz) {
        T options = CliFactory.parseArguments(clazz, args);
        return options;
    }

}
