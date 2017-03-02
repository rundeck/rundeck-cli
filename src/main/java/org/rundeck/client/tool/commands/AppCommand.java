package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.AppConfig;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by greg on 5/23/16.
 */
public abstract class AppCommand implements RdApp {
    private final RdApp rdApp;

    public AppCommand(final RdApp rdApp) {
        this.rdApp = rdApp;
    }

    public Client<RundeckApi> getClient() throws InputError {
        return rdApp.getClient();
    }

    public AppConfig getAppConfig() {
        return rdApp.getAppConfig();
    }

    public <T> T apiCall(Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        Client<RundeckApi> client = getClient();
        return apiCall(client, func);
    }

    public static <T> T apiCall(
            final Client<RundeckApi> client,
            final Function<RundeckApi, Call<T>> func
    )
            throws IOException
    {
        return client.checkError(func.apply(client.getService()));
    }

    public String projectOrEnv(final ProjectNameOptions options) throws InputError {
        if (null != options.getProject()) {
            return options.getProject();
        }
        return getAppConfig().require("RD_PROJECT", "or specify as `-p/--project value` : Project name.");
    }

}
