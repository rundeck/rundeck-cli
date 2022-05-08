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


import lombok.Data;
import org.rundeck.client.tool.extension.BaseCommand;
import picocli.CommandLine;
import org.rundeck.client.api.model.scheduler.*;
import org.rundeck.client.tool.options.VerboseOption;


import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.RdApp;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * scheduler subcommands
 */
@CommandLine.Command(description = "View scheduler information", name = "scheduler")
public class Scheduler extends BaseCommand {


  @Data
  static class SchedulerJobs {

    @CommandLine.Option(names = {"-u", "--uuid"},
            description = "Server UUID to query, or blank to select the target server")
    private String uuid;

    boolean isUuid() {
      return uuid != null;
    }
  }

  @CommandLine.Command(description = "List jobs for the current target server, or a specified server.")
  public void jobs(@CommandLine.Mixin SchedulerJobs options) throws IOException, InputError {
    List<ScheduledJobItem> jobInfo = apiCall(api -> {
      if (options.isUuid()) {
        return api.listSchedulerJobs(options.getUuid());
      } else {
        return api.listSchedulerJobs();
      }

    });
    getRdOutput().output(jobInfo.stream().map(ScheduledJobItem::toMap).collect(Collectors.toList()));
  }


  @Data
  static class Takeover extends VerboseOption {

    @CommandLine.Option(names = {"-u", "--uuid"},
            description = "Server UUID to take over.")
    String uuid;

    boolean isUuid() {
      return uuid != null;
    }

    @CommandLine.Option(names = {"-a", "--all"},
            description = "Take over all jobs regardless of server UUID.")
    boolean allServers;

    @CommandLine.Option(names = {"-p", "--project"},
            description = "Take over only jobs matching the given project name, in combination with --all/-a or --uuid/-u.")
    String projectName;

    boolean isProjectName() {
      return projectName != null;
    }

    @CommandLine.Option(names = {"-j", "--job"},
            description = "Job UUID to takeover only a single Job’s schedule.")
    String jobId;

    boolean isJobId() {
      return jobId != null;
    }

  }

  @CommandLine.Command(description = "Tell a Rundeck server in cluster mode to claim all scheduled jobs from another cluster server. " +
          "Use --job/-j to specify a job uuid, or alternatively use --uuid/-u, --all/-a, --project/-p to " +
          "specify a server/project combination.")
  public void takeover(@CommandLine.Mixin Takeover options) throws IOException, InputError {

    SchedulerTakeover takeoverParams = new SchedulerTakeover();

    if (options.isJobId()) {
      // Takeover by job uuid.
      takeoverParams.setJob(new SchedulerTakeover.JobId()
              .setId(options.getJobId())
      );
    }

    else {
      // Takeover by server params.
      if (options.isAllServers()) {
        takeoverParams.setServer(new TakeoverServerItem()
            .setAll(true));
      }
      else if (options.isUuid()) {
        takeoverParams.setServer(new TakeoverServerItem()
            .setUuid(options.getUuid()));
      }
      else {
        throw new InputError("Must specify -u, or -a or -j to specify jobs to takeover.");
      }

      if (options.isProjectName()) {
        takeoverParams.setProject(options.getProjectName());
      }
    }

//    getRdOutput().info(takeoverParams.toString());
    SchedulerTakeoverResult response = apiCall(api -> api.takeoverSchedule(takeoverParams));

    // print getRdOutput().
    getRdOutput().info(response.getMessage());

    if (response.getTakeoverSchedule() != null
            && response.getTakeoverSchedule().getJobs() != null
            && response.getTakeoverSchedule().getJobs().getFailed() != null
            && response.getTakeoverSchedule().getJobs().getFailed().size() > 0) {

      getRdOutput().error(String.format("Failed to takeover %d Jobs%n", response.getTakeoverSchedule().getJobs().getFailed().size()));
      getRdOutput().output(response.getTakeoverSchedule().getJobs().getFailed().stream()
              .map(TakeoverJobItem::toString)
              .collect(Collectors.toList()));
    }

    if (options.isVerbose()) {
      if (response.getTakeoverSchedule() != null
              && response.getTakeoverSchedule().getJobs() != null
          && response.getTakeoverSchedule().getJobs().getSuccessful() != null
          && response.getTakeoverSchedule().getJobs().getSuccessful().size() > 0) {

        getRdOutput().info(String.format("Successfully taken over %d Jobs%n", response.getTakeoverSchedule().getJobs().getSuccessful().size()));
        getRdOutput().output(response.getTakeoverSchedule().getJobs().getSuccessful().stream()
                .map(TakeoverJobItem::toString)
                .collect(Collectors.toList()));
      }
      else {
        getRdOutput().warning("* No jobs were taken over.");
      }
    }
  }

}
