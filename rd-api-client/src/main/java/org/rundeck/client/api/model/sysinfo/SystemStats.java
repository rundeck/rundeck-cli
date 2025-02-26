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

package org.rundeck.client.api.model.sysinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemStats {
    private Map<String, Object> timestamp;
    private Map<String, Object> rundeck;
    private Map<String, Object> executions;
    private Map<String, Object> os;
    private Map<String, Object> jvm;
    private Map<String, Map> stats;
    private Link metrics;
    private Link threadDump;
    private Link healthcheck;

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        if(null!=timestamp) {
            data.put("timestamp", timestamp);
        }
        if(null!=rundeck) {
            data.put("rundeck", rundeck);
        }
        if(null!=executions) {
            data.put("executions", executions);
        }
        if(null!=os) {
            data.put("os", os);
        }
        if (jvm != null) {
            data.put("jvm", jvm);
        }
        if (stats != null) {
            data.put("stats", stats);
        }
        if (metrics != null) {
            data.put("metrics", metrics.toMap());
        }
        if (threadDump != null) {
            data.put("threadDump", threadDump.toMap());
        }
        if (healthcheck != null) {
            data.put("healthcheck", healthcheck.toMap());
        }
        return data;
    }

    @Override
    public String toString() {
        return "{" + "\n" +
               "timestamp=" + timestamp + "\n" +
               ", rundeck=" + rundeck + "\n" +
               ", executions=" + executions + "\n" +
               ", os=" + os + "\n" +
               ", jvm=" + jvm + "\n" +
               ", stats=" + stats + "\n" +
               ", metrics=" + metrics + "\n" +
               ", threadDump=" + threadDump + "\n" +
               ", healthcheck=" + healthcheck + "\n" +
               '}';
    }
}
