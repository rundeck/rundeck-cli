/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.AppConfig;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.ServiceClient;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

/**
 * Base type for commands in Rd
 */
public abstract class AppCommand  {
    private final RdApp rdApp;

    public AppCommand(final RdApp rdApp) {
        this.rdApp = rdApp;
    }

    public ServiceClient<RundeckApi> getClient() throws InputError {
        return rdApp.getClient();
    }

    public AppConfig getAppConfig() {
        return rdApp.getAppConfig();
    }

    /**
     * Perform a downgradable API call
     *
     * @param func function
     * @param <T>  result type
     *
     * @return result
     *
     * @throws InputError  on error
     * @throws IOException on error
     */
    public <T> T apiCall(Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        return apiCallDowngradable(rdApp, func);
    }

    /**
     * Perform API call with a client
     *
     * @param client client
     * @param func   function
     * @param <T>    result type
     *
     * @return result
     *
     * @throws IOException on error
     */
    public static <T> T apiCall(
            final ServiceClient<RundeckApi> client,
            final Function<RundeckApi, Call<T>> func
    )
            throws IOException
    {
        return client.apiCall(func);
    }

    /**
     * Perform a downgradable api call
     *
     * @param rdApp app
     * @param func  function
     * @param <T>   result type
     *
     * @return result
     *
     * @throws InputError  on error
     * @throws IOException on error
     */
    public static <T> T apiCallDowngradable(
            final RdApp rdApp,
            final Function<RundeckApi, Call<T>> func
    )
            throws InputError, IOException
    {
        try {
            return rdApp.getClient().apiCallDowngradable(func);
        } catch (Client.UnsupportedVersionDowngrade downgrade) {
            //downgrade to supported version and try again
            rdApp.versionDowngradeWarning(
                    downgrade.getRequestedVersion(),
                    downgrade.getSupportedVersion()
            );
            return rdApp.getClient(downgrade.getSupportedVersion()).apiCall(func);
        }
    }

    /**
     * @param options project options
     *
     * @return project name from options or ENV
     *
     * @throws InputError if project is not set via options or ENV
     */
    public String projectOrEnv(final ProjectNameOptions options) throws InputError {
        if (null != options.getProject()) {
            return options.getProject();
        }
        return getAppConfig().require("RD_PROJECT", "or specify as `-p/--project value` : Project name.");
    }

    public RdApp getRdApp() {
        return rdApp;
    }

    /**
     * Supplier with throwable
     *
     * @param <T> type
     */
    interface GetInput<T> {
        /**
         * @return supplied input
         * @throws InputError if input error
         */
        T get() throws InputError;
    }

}
