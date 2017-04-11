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
public class BulkExecutionDeleteResponse {
    private int failedCount;
    private int successCount;
    private boolean allsuccessful;
    private int requestCount;
    private List<DeleteFailure> failures;

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

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

    public List<DeleteFailure> getFailures() {
        return failures;
    }

    public void setFailures(List<DeleteFailure> failures) {
        this.failures = failures;
    }

    @Override
    public String toString() {
        return "org.rundeck.client.api.model.BulkExecutionDeleteResponse{" +
               "failedCount=" + failedCount +
               ", successCount=" + successCount +
               ", allsuccessful=" + allsuccessful +
               ", requestCount=" + requestCount +
               ", failures=" + failures +
               '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteFailure {
        private String id;
        private String message;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("* #%s: '%s'", id, message);
        }
    }
}
