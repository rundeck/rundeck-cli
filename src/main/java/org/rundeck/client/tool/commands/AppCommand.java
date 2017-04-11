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
import retrofit2.Call;

import java.io.IOException;
import java.util.function.Function;

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
            throws InputError, IOException
    {
        return client.checkError(func.apply(client.getService()));
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
