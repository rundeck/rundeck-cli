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

import lombok.Getter;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.ProjectInput;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdOutput;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.ConfigSource;
import org.rundeck.client.util.RdClientConfig;
import org.rundeck.client.util.ServiceClient;
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.rundeck.client.tool.ProjectInput.PROJECT_NAME_PATTERN;

/**
 * Base type for commands in Rd todo: rename
 */
public class RdToolImpl
        implements RdTool
{
    @Getter private final RdApp rdApp;

    public RdToolImpl(final RdApp rdApp) {
        this.rdApp = rdApp;
    }

    public <T extends RdCommandExtension> T initExtension(final T extension) {
        extension.setRdTool(this);
        if (extension instanceof RdOutput) {
            ((RdOutput) extension).setRdOutput(rdApp.getOutput());
        }
        return extension;
    }

    public ServiceClient<RundeckApi> getClient() throws InputError {
        return getRdApp().getClient();
    }

    public RdClientConfig getAppConfig() {
        return getRdApp().getAppConfig();
    }


    /**
     * Perform a downgradable API call
     *
     * @param func function
     * @param <T>  result type
     * @return result
     * @throws InputError  on error
     * @throws IOException on error
     */
    public <T> T apiCall(Function<RundeckApi, Call<T>> func) throws InputError, IOException {
        return apiCallDowngradable(func);
    }

    /**
     * Perform a downgradable API call without handling errors in response
     *
     * @param func function
     * @param <T>  result type
     * @return response
     * @throws InputError  on error
     * @throws IOException on error
     */
    public <T> ServiceClient.WithErrorResponse<T> apiWithErrorResponse(Function<RundeckApi, Call<T>> func)
            throws InputError, IOException
    {
        return apiWithErrorResponseDowngradable(func);
    }

    /**
     * Perform a downgradable api call
     *
     * @param func function
     * @param <T>  result type
     * @return result
     * @throws InputError  on error
     * @throws IOException on error
     */
    public <T> T apiCallDowngradable(final Function<RundeckApi, Call<T>> func) throws InputError, IOException {
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

    public <T> ServiceClient.WithErrorResponse<T> apiWithErrorResponseDowngradable(
            final Function<RundeckApi,
                    Call<T>> func
    )
            throws InputError, IOException
    {
        try {
            return rdApp.getClient().apiWithErrorResponseDowngradable(func);
        } catch (Client.UnsupportedVersionDowngrade downgrade) {
            //downgrade to supported version and try again
            rdApp.versionDowngradeWarning(
                    downgrade.getRequestedVersion(),
                    downgrade.getSupportedVersion()
            );
            return rdApp.getClient(downgrade.getSupportedVersion()).apiWithErrorResponse(func);
        }
    }

    /**
     * @param options project options
     * @return project name from options or ENV
     * @throws InputError if project is not set via options or ENV
     */
    public String projectOrEnv(final ProjectInput options) throws InputError {
        if (null != options.getProject()) {
            return options.getProject();
        }
        try {
            String rd_project =
                    getAppConfig().require("RD_PROJECT", "or specify as `-p/--project value` : Project name.");
            Pattern pat = Pattern.compile(PROJECT_NAME_PATTERN);
            if (!pat.matcher(rd_project).matches()) {
                throw new InputError(String.format(
                        "Cannot match (%s) to pattern: /%s/ : RD_PROJECT",
                        rd_project,
                        PROJECT_NAME_PATTERN
                ));
            }
            return rd_project;
        } catch (ConfigSource.ConfigSourceError configSourceError) {
            throw new InputError(configSourceError.getMessage());
        }
    }

    public void requireApiVersion(final String description, final int min) throws InputError {
        RdTool.apiVersionCheck(description, min, getClient().getApiVersion());
    }

}
