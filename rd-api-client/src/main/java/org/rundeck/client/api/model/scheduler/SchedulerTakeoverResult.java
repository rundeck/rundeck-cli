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

package org.rundeck.client.api.model.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SchedulerTakeoverResult {


  private String           message;
  private int              apiversion;
  private boolean          success;
  private TakeoverSchedule takeoverSchedule;
  private TakeoverSelf     self;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TakeoverSelf {
    private TakeoverServerItem server;

    public TakeoverServerItem getServer() {
      return server;
    }

    public TakeoverSelf setServer(TakeoverServerItem server) {
      this.server = server;
      return this;
    }

    @Override
    public String toString() {
      return "{" +
          "server=" + server +
          '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TakeoverSchedule {
    private TakeoverServerItem server;
    private String             project;
    private JobTakeoverResult  jobs;

    public TakeoverServerItem getServer() {
      return server;
    }

    public TakeoverSchedule setServer(TakeoverServerItem server) {
      this.server = server;
      return this;
    }

    public String getProject() {
      return project;
    }

    public TakeoverSchedule setProject(String project) {
      this.project = project;
      return this;
    }

    public JobTakeoverResult getJobs() {
      return jobs;
    }

    public TakeoverSchedule setJobs(JobTakeoverResult jobs) {
      this.jobs = jobs;
      return this;
    }

    @Override
    public String toString() {
      return "{" +
          "server=" + server +
          ", project='" + project + '\'' +
          ", jobs=" + jobs +
          '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class JobTakeoverResult {
    private int                   total;
    private List<TakeoverJobItem> successful;
    private List<TakeoverJobItem> failed;

    public int getTotal() {
      return total;
    }

    public JobTakeoverResult setTotal(int total) {
      this.total = total;
      return this;
    }

    public List<TakeoverJobItem> getSuccessful() {
      return successful;
    }

    public JobTakeoverResult setSuccessful(List<TakeoverJobItem> successful) {
      this.successful = successful;
      return this;
    }

    public List<TakeoverJobItem> getFailed() {
      return failed;
    }

    public JobTakeoverResult setFailed(List<TakeoverJobItem> failed) {
      this.failed = failed;
      return this;
    }

    @Override
    public String toString() {
      return "{" +
          "total=" + total +
          ", successful=" + successful +
          ", failed=" + failed +
          '}';
    }
  }

  public String getMessage() {
    return message;
  }

  public SchedulerTakeoverResult setMessage(String message) {
    this.message = message;
    return this;
  }

  public int getApiversion() {
    return apiversion;
  }

  public SchedulerTakeoverResult setApiversion(int apiversion) {
    this.apiversion = apiversion;
    return this;
  }

  public boolean isSuccess() {
    return success;
  }

  public SchedulerTakeoverResult setSuccess(boolean success) {
    this.success = success;
    return this;
  }

  public TakeoverSchedule getTakeoverSchedule() {
    return takeoverSchedule;
  }

  public SchedulerTakeoverResult setTakeoverSchedule(TakeoverSchedule takeoverSchedule) {
    this.takeoverSchedule = takeoverSchedule;
    return this;
  }

  public TakeoverSelf getSelf() {
    return self;
  }

  public SchedulerTakeoverResult setSelf(TakeoverSelf self) {
    this.self = self;
    return this;
  }

  @Override
  public String toString() {
    return "SchedulerTakeoverResult{" +
        "message='" + message + '\'' +
        ", apiversion=" + apiversion +
        ", success=" + success +
        ", takeoverSchedule=" + takeoverSchedule +
        ", self=" + self +
        '}';
  }
}
