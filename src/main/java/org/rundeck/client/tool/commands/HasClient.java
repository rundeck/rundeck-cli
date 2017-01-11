package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.App;
import org.rundeck.client.util.Client;

/**
 * @author greg
 * @since 1/11/17
 */
public interface HasClient {
    public Client<RundeckApi> getClient() throws InputError;

    public App.AppConfig getAppConfig();
}
