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

package org.rundeck.client.tool;

import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.util.RdClientConfig;
import org.rundeck.client.util.ServiceClient;

/**
 * Access to config, output, and service client
 * @author greg
 * @since 1/11/17
 */
public interface RdApp {
    /**
     * @return current service client
     *
     * @throws InputError on error
     */
    ServiceClient<RundeckApi> getClient() throws InputError;

    /**
     * @param version api version to use
     *
     * @return service client for particular api version
     *
     * @throws InputError on erro
     */
    ServiceClient<RundeckApi> getClient(int version) throws InputError;

    /**
     * @return app config
     */
    RdClientConfig getAppConfig();

    /**
     * @return output endpoint
     */
    public CommandOutput getOutput();

    /**
     * Issue warning about api version downgrade
     * @param requested requested api version
     * @param supported supported api verson
     */
    public void versionDowngradeWarning(int requested, int supported);
}
