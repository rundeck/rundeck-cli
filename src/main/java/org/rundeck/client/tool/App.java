package org.rundeck.client.tool;

import org.rundeck.client.api.model.ProjectItem;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by greg on 3/28/16.
 */
public class App {
    public static void main(String[] args) throws IOException {
        if (args[0].equals("jobs")) {
            String[] newargs = tail(args);
            Jobs.main(newargs);
        } else if (args[0].equals("projects")) {
            String[] newargs = tail(args);
            Projects.main(newargs);
        }
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

    public static <T> T checkError(final Call<T> execute) throws IOException {
        Response<T> response = execute.execute();
        if (!response.isSuccess()) {
            if (response.code() == 401 || response.code() == 403) {
                //authorization
                throw new AuthorizationFailed(
                        String.format("Authorization failed: %d %s", response.code(), response.message()),
                        response.code(),
                        response.message()
                );
            }
            if (response.code() == 409) {
                //authorization
                throw new RequestFailed(String.format(
                        "Could not create resource: %d %s",
                        response.code(),
                        response.message()
                ), response.code(), response.message());
            }
            if (response.code() == 404) {
                //authorization
                throw new RequestFailed(String.format(
                        "Could not find resource:  %d %s",
                        response.code(),
                        response.message()
                ), response.code(), response.message());
            }
            throw new RequestFailed(
                    String.format("Request failed:  %d %s", response.code(), response.message()),
                    response.code(),
                    response.message()
            );
        }
        return response.body();
    }
}
