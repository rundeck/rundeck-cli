package org.rundeck.client.tool.extension;

import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.ProjectInput;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.util.RdClientConfig;
import org.rundeck.client.util.ServiceClient;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

/**
 * Interface to the RD tool
 */
public interface RdTool {
    <T extends RdCommandExtension> T initExtension(final T extension);

    ServiceClient<RundeckApi> getClient() throws InputError;

    RdClientConfig getAppConfig();

    /**
     * Perform a downgradable API call
     *
     * @param func function
     * @param <T>  result type
     * @return result
     * @throws InputError  on error
     * @throws IOException on error
     */
    <T> T apiCall(Function<RundeckApi, Call<T>> func) throws InputError, IOException;

    /**
     * Perform a downgradable API call without handling errors in response
     *
     * @param func function
     * @param <T>  result type
     * @return response
     * @throws InputError  on error
     * @throws IOException on error
     */
    <T> ServiceClient.WithErrorResponse<T> apiWithErrorResponse(Function<RundeckApi, Call<T>> func)
            throws InputError, IOException;


    /**
     * Perform a downgradable api call
     *
     * @param func function
     * @param <T>  result type
     * @return result
     * @throws InputError  on error
     * @throws IOException on error
     */
    <T> T apiCallDowngradable(
            final Function<RundeckApi, Call<T>> func
    )
            throws InputError, IOException;

    /**
     * Perform a downgradable api call, without handling errors
     *
     * @param func function
     * @param <T>  result type
     * @return response
     * @throws InputError  on error
     * @throws IOException on error
     */
    <T> ServiceClient.WithErrorResponse<T> apiWithErrorResponseDowngradable(
            final Function<RundeckApi, Call<T>> func
    )
            throws InputError, IOException;

    /**
     * @param options project options
     * @return project name from options or ENV
     * @throws InputError if project is not set via options or ENV
     */
    String projectOrEnv(final ProjectInput options) throws InputError;

    RdApp getRdApp();

    /**
     * Require the client to have minimum API version
     *
     * @param description reason for the requirement
     * @param min         required minimum version
     * @throws InputError if version is not met
     */
    void requireApiVersion(final String description, final int min) throws InputError;

    /**
     * Require the client to have minimum API version
     *
     * @param description    reason for the requirement
     * @param min            required minimum version
     * @param currentVersion current version
     * @throws InputError if version is not met
     */
    static void apiVersionCheck(String description, int min, int currentVersion) throws InputError {
        if (currentVersion < min) {
            throw new InputError(String.format(
                    "%s: requires API >= %d (current: %d)",
                    description,
                    min,
                    currentVersion
            ));
        }
    }
}
