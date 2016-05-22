package org.rundeck.client.tool;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.AuthorizationFailed;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ErrorResponse;
import org.rundeck.client.util.Client;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    public static void main(String[] args) throws IOException {
        String[] commands = new String[]{
                "projects",
                "jobs",
                "executions",
                "adhoc",
                "run"
        };
        if(args.length<1){
            System.err.printf("Available commands: %s%n", Arrays.asList(commands));
            System.exit(2);
        }
        if (args[0].equals("jobs")) {
            Jobs.main(tail(args));
        } else if (args[0].equals("projects")) {
            Projects.main(tail(args));
        } else if (args[0].equals("executions")) {
            Executions.main(tail(args));
        } else if (args[0].equals("adhoc")) {
            Adhoc.main(tail(args));
        }else if (args[0].equals("run")) {
            Run.main(tail(args));
        } else {
            throw new IllegalArgumentException("Unknown command: " + args[0]);
        }
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

}
