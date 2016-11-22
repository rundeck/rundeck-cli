package org.rundeck.client.tool.commands;

import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.util.Client;

import java.util.function.Supplier;

/**
 * Created by greg on 5/23/16.
 */
public abstract class ApiCommand {
    private Client<RundeckApi> client;
    private final Supplier<Client<RundeckApi>> builder;


    public ApiCommand(final Supplier<Client<RundeckApi>> builder) {
        this.builder = builder;
        this.client = null;
    }

    public Client<RundeckApi> getClient() {
        if (null == client && null != builder) {
            client = builder.get();
        }
        return client;
    }
}
