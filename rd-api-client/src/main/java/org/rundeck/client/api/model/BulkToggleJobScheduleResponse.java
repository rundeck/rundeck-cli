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
public class BulkToggleJobScheduleResponse {
  private int          requestCount;
  private boolean      enabled;
  private boolean      allsuccessful;
  private List<Result> succeeded;
  private List<Result> failed;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Result {
    private String id;
    private String errorCode;
    private String message;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getErrorCode() {
      return errorCode;
    }

    public void setErrorCode(String errorCode) {
      this.errorCode = errorCode;
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

  public int getRequestCount() {
    return requestCount;
  }

  public void setRequestCount(int requestCount) {
    this.requestCount = requestCount;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isAllsuccessful() {
    return allsuccessful;
  }

  public void setAllsuccessful(boolean allsuccessful) {
    this.allsuccessful = allsuccessful;
  }

  public List<Result> getSucceeded() {
    return succeeded;
  }

  public void setSucceeded(List<Result> succeeded) {
    this.succeeded = succeeded;
  }

  public List<Result> getFailed() {
    return failed;
  }

  public void setFailed(List<Result> failed) {
    this.failed = failed;
  }

  @Override
  public String toString() {
    return "BulkToggleJobScheduleResponse{" +
        "requestCount=" + requestCount +
        ", enabled=" + enabled +
        ", allsuccessful=" + allsuccessful +
        ", succeeded=" + succeeded +
        ", failed=" + failed +
        '}';
  }
}
