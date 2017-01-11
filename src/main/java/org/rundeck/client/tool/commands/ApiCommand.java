package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.App;
import org.rundeck.client.tool.options.EnvOptions;
import org.rundeck.client.tool.options.OptionUtil;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by greg on 5/23/16.
 */
public abstract class ApiCommand implements EnvOptions, HasClient {
    private Client<RundeckApi> client;
    private final App.AppConfig appConfig;
    private final HasClient builder;
    private OptionUtil envOptions = new OptionUtil();

    @Override
    public String projectOrEnv(final ProjectNameOptions options) throws InputError {
        return envOptions.projectOrEnv(options, appConfig);
    }

    public App.AppConfig getAppConfig() {
        return appConfig;
    }

    public ApiCommand(final HasClient builder) {
        this.builder = builder;
        this.client = null;
        this.appConfig = builder.getAppConfig();

    }

    public Client<RundeckApi> getClient() throws InputError {
        if (null == client && null != builder) {
            client = builder.getClient();
        }
        return client;
    }

    public <T> T apiCall(Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        return getClient().checkError(func.apply(getClient().getService()));
    }
}
