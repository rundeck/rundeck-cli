package org.rundeck.client.tool.commands;

import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.util.Client;

/**
 * Created by greg on 5/23/16.
 */
public abstract class ApiCommand {
    protected final Client<RundeckApi> client;

    public ApiCommand(final Client<RundeckApi> client) {
        this.client = client;
    }
}
