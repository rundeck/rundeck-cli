package org.rundeck.client.testing

import org.rundeck.client.api.RundeckApi
import org.rundeck.client.tool.InputError
import org.rundeck.client.tool.ProjectInput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.extension.RdCommandExtension
import org.rundeck.client.tool.extension.RdOutput
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.util.RdClientConfig
import org.rundeck.client.util.ServiceClient
import retrofit2.Call

import java.util.function.Function

class MockRdTool implements RdTool {
    RdApp rdApp
    RdClientConfig appConfig
    ServiceClient<RundeckApi> client;

    @Override
    def <T extends RdCommandExtension> T initExtension(final T extension) {
        extension.setRdTool(this)
        if (extension instanceof RdOutput) {
            ((RdOutput) extension).rdOutput = rdApp.output
        }
        extension
    }


    @Override
    def <T> T apiCall(final Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        func.apply(client.service).execute().body()
    }

    @Override
    def <T> ServiceClient.WithErrorResponse<T> apiWithErrorResponse(final Function<RundeckApi, Call<T>> func)
        throws InputError, IOException {
        client.apiWithErrorResponse(func)
    }

    @Override
    def <T> T apiCallDowngradable(final Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        func.apply(client.service).execute().body()
    }

    @Override
    def <T> ServiceClient.WithErrorResponse<T> apiWithErrorResponseDowngradable(
        final Function<RundeckApi, Call<T>> func
    )
        throws InputError, IOException {
        client.apiWithErrorResponse(func)
    }

    @Override
    String projectOrEnv(final ProjectInput options) throws InputError {
        return options.project
    }

    @Override
    void requireApiVersion(final String description, final int min) throws InputError {
        RdTool.apiVersionCheck(description, min, getClient().getApiVersion())
    }
}
