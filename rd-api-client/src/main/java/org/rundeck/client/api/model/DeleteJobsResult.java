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

package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteJobsResult {
    private boolean allsuccessful;
    private int requestCount;
    private List<DeleteJob> succeeded;
    private List<DeleteJob> failed;

    public boolean isAllsuccessful() {
        return allsuccessful;
    }

    public void setAllsuccessful(boolean allsuccessful) {
        this.allsuccessful = allsuccessful;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public List<DeleteJob> getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(List<DeleteJob> succeeded) {
        this.succeeded = succeeded;
    }

    public List<DeleteJob> getFailed() {
        return failed;
    }

    public void setFailed(List<DeleteJob> failed) {
        this.failed = failed;
    }
}
