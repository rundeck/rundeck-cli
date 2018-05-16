/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
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

package org.rundeck.client.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;



/**
 * Parameters to run a job
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecRetry extends JobRun{
    private String failedNodes;

    public String getFailedNodes() {
        return failedNodes;
    }

    public void setFailedNodes(String failedNodes) {
        this.failedNodes = failedNodes;
    }

    @Override
    public String toString() {
        if (null != failedNodes) {
            return "org.rundeck.client.api.model.ExecRetry{" +
                    "failedNodes='" + failedNodes + '\'' +
                    '}';
        } else {
            return super.toString();
        }
    }
}
