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

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchedulerTakeover {

  private TakeoverServerItem server;
  private String project;
  private JobId  job;


  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class JobId {
    public String id;

    public String getId() {
      return id;
    }

    public JobId setId(String id) {
      this.id = id;
      return this;
    }

    @Override
    public String toString() {
      return "{" +
          "id='" + id + '\'' +
          '}';
    }
  }

  // getters setters

  public TakeoverServerItem getServer() {
    return server;
  }

  public SchedulerTakeover setServer(TakeoverServerItem server) {
    this.server = server;
    return this;
  }

  public String getProject() {
    return project;
  }

  public SchedulerTakeover setProject(String project) {
    this.project = project;
    return this;
  }

  public JobId getJob() {
    return job;
  }

  public SchedulerTakeover setJob(JobId job) {
    this.job = job;
    return this;
  }

  @Override
  public String toString() {
    return "SchedulerTakeover{" +
        "server=" + server +
        ", project='" + project + '\'' +
        ", job=" + job +
        '}';
  }
}
