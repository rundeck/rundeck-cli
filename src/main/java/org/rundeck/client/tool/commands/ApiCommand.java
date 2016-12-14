package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.util.Client;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by greg on 5/23/16.
 */
public abstract class ApiCommand {
    private Client<RundeckApi> client;
    private final HasClient builder;

    public static interface HasClient {
        public Client<RundeckApi> getClient() throws InputError;
    }

    public ApiCommand(final HasClient builder) {
        this.builder = builder;
        this.client = null;
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
