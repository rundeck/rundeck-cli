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

package org.rundeck.client.api.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthCheckStatus {

    private boolean healthy;
    private String message;

    public boolean isHealthy() {
        return healthy;
    }

    public HealthCheckStatus setHealthy(boolean healthy) {
        this.healthy = healthy;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public HealthCheckStatus setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "HealthCheckStatus{" +
            "healthy=" + healthy +
            ", message='" + message + '\'' +
            '}';
    }
}
