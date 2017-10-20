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

import com.simplifyops.toolbelt.CommandOutput;
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

public abstract class AppCommand implements RdApp {
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

    @Override
    public CommandOutput getOutput() {
        return rdApp.getOutput();
    }

    @Override
    public ServiceClient<RundeckApi> getClient(final int version) throws InputError {
        return rdApp.getClient(version);
    }

    @Override
    public void versionDowngradeWarning(final int requested, final int supported) {
        rdApp.versionDowngradeWarning(requested, supported);
    }

    public <T> T apiCall(Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        try {
            return getClient().apiCallDowngradable(func);
        } catch (Client.UnsupportedVersion unsupportedVersion) {
            //degrade to supported version and try again
            if (unsupportedVersion.getLatestVersion() < unsupportedVersion.getRequestedVersion()) {
                rdApp.versionDowngradeWarning(
                        unsupportedVersion.getRequestedVersion(),
                        unsupportedVersion.getLatestVersion()
                );
                return rdApp.getClient(unsupportedVersion.getLatestVersion()).apiCall(func);
            } else {
                throw unsupportedVersion.getRequestFailed();
            }
        }
    }

    public static <T> T apiCall(
            final ServiceClient<RundeckApi> client,
            final Function<RundeckApi, Call<T>> func
    )
            throws IOException
    {
        return client.apiCall(func);
    }

    public static <T> T apiCallDowngradable(
            final ServiceClient<RundeckApi> client,
            final Function<RundeckApi, Call<T>> func
    )
            throws IOException, Client.UnsupportedVersion
    {
        return client.apiCallDowngradable(func);
    }

    public String projectOrEnv(final ProjectNameOptions options) throws InputError {
        if (null != options.getProject()) {
            return options.getProject();
        }
        return getAppConfig().require("RD_PROJECT", "or specify as `-p/--project value` : Project name.");
    }

    interface GetInput<T> {
        T get() throws InputError;
    }

}
