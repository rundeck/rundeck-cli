package org.rundeck.client.tool;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.AppConfig;
import org.rundeck.client.util.Client;

/**
 * @author greg
 * @since 1/11/17
 */
public interface RdApp {
    public Client<RundeckApi> getClient() throws InputError;

    public AppConfig getAppConfig();
}
