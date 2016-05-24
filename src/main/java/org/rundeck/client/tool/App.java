package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.InvalidOptionSpecificationException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.belt.*;
import org.rundeck.client.tool.commands.*;
import org.rundeck.client.util.Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        return ToolBuilder.builder().addCommands(command).setParser(new JewelInput()).build();
    }

    public static Tool tool() {
        Client<RundeckApi> client = createClient();
        return ToolBuilder.builder()
                          .defaultHelp()
                          .systemOutput()
                          .addCommands(
                                  new Adhoc(client),
                                  new Jobs(client),
                                  new Projects(client),
                                  new Executions(client),
                                  new Run(client)
                          )
                          .setParser(new JewelInput())
                          .build();
    }


    public static Client<RundeckApi> createClient() {
        String baseUrl = requireEnv("RUNDECK_URL", "Please specify the Rundeck URL");
        String token = requireEnv("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
        return Rundeck.client(baseUrl, token, System.getenv("DEBUG") != null);
    }

    public static String[] tail(final String[] args) {
        List<String> strings = new ArrayList<>(Arrays.asList(args));
        strings.remove(0);
        return strings.toArray(new String[strings.size()]);
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

    public static class JewelInput implements CommandInput {
        @Override
        public <T> T parseArgs(final String[] args, final Class<? extends T> clazz) throws InputError {
            try {
                return CliFactory.parseArguments(clazz, args);
            } catch (ArgumentValidationException e) {
                throw new InputError(e.getMessage(),e);
            }
        }

        @Override
        public String getHelp(final Class<?> type) {
            Cli<?> cli = CliFactory.createCli(type);
            return cli.getHelpMessage();
        }
    }
}
