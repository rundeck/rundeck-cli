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
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.metrics.EndpointListResult;
import org.rundeck.client.api.model.metrics.HealthCheckStatus;
import org.rundeck.client.api.model.metrics.MetricsData;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.VerboseOption;


import org.rundeck.client.tool.InputError;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * scheduler subcommands
 */
@CommandLine.Command(description = "View metrics endpoints information.", name = "metrics")
public class Metrics extends BaseCommand {


  // rd metrics list

  @CommandLine.Command(description = "Print system information and stats.")
  public void list(@CommandLine.Mixin VerboseOption options) throws IOException, InputError {

    EndpointListResult endpointList = apiCall(RundeckApi::listMetricsEndpoints);

    if (endpointList.size() > 0) {
      getRdOutput().info(endpointList.size() + " metric endpoints:");
      getRdOutput().output(endpointList.getEndpointLinks().entrySet().stream()
              .map(metricEntry -> metricEntry.getKey() +
                      (options.isVerbose() ? (" " + metricEntry.getValue().getHref()) : ""))
              .collect(Collectors.joining("\n", "", "\n"))
      );
    } else {
      getRdOutput().warning("No metrics endpoints found.");
    }
  }


  // rd metrics healthcheck

  @Data
  static class HealthCheckOptions {
    @CommandLine.Option(names = {"-u", "--unhealthy"},
            description = "Show only checks with unhealthy status.")
    boolean unhealthyOnly;

    @CommandLine.Option(names = {"-f", "--fail"},
            description = "Exit with unsuccessful status if unhealthy checks are found.")
    boolean failOnUnhealthy;

  }

  @CommandLine.Command(description = "Print health check status information.")
  public boolean healthcheck(@CommandLine.Mixin HealthCheckOptions options) throws IOException, InputError {

    Map<String, HealthCheckStatus> healthCheckStatus = apiCall(RundeckApi::getHealthCheckMetrics);

    // Obtain unhealthy list.
    Map<String, HealthCheckStatus> unhealthyList = healthCheckStatus.entrySet().stream()
            .filter(entry -> !entry.getValue().isHealthy())
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue));

    Map<String, HealthCheckStatus> statusList = options.isUnhealthyOnly() ? unhealthyList : healthCheckStatus;

    if (statusList.size() > 0) {
      getRdOutput().info("Showing current health status for " + statusList.size() + " checks:");

      getRdOutput().output(statusList.entrySet().stream()
              .map(healthCheckEntry -> String.format("%-11s %s%s",
                      (healthCheckEntry.getValue().isHealthy() ? "HEALTHY" : "NOT-HEALTHY"),
                      healthCheckEntry.getKey(),
                      (healthCheckEntry.getValue().getMessage() != null ? " -- " + healthCheckEntry.getValue().getMessage() : "")
              ))
              .collect(Collectors.joining(
                      "\n",
                      "",
                      ""
              ))
      );
    } else {
      getRdOutput().warning("No results found.");
    }

    return !options.isFailOnUnhealthy() || unhealthyList.size() == 0;
  }

  // rd metrics threads

  @CommandLine.Command(description = "Print system threads status information.")
  public void threads(@CommandLine.Mixin VerboseOption options) throws IOException, InputError {

    ResponseBody response = apiCall(RundeckApi::getThreadMetrics);

    getRdOutput().info("System threads status: ");
    if (options.isVerbose()) {
      getRdOutput().output(response.string());
    } else {
      getRdOutput().output(Stream.of(response.string().split("\n"))
              .filter(s -> s.matches("^\\S+.*"))
              .collect(Collectors.joining("\n")));
    }
  }

  // rd metrics ping

//  @CommandLineInterface(application = "ping")
//  interface PingOptions {
//
//  }

  @CommandLine.Command(description = "Returns a simple response.")
  public void ping() throws IOException, InputError {

    getRdOutput().info(printTimestamp() + "Pinging server...");
    try (ResponseBody response = apiCall(RundeckApi::getPing)) {
      getRdOutput().info(printTimestamp() + response.string());
    }
  }

  /**
   * @return A formatted string with the current local time. Such as "[2007-12-03T10:15:30] ".
   */
  private String printTimestamp() {
    return "[" + LocalDateTime.now().toString() + "] ";
  }


  // rd metrics data

  @Data
  static class MetricsDataOptions {

    @CommandLine.Option(names = {"-s", "--summary"},
            description = "Show only a summary of metric data selected."
    )
    boolean summary;

    @CommandLine.Option(names = {"-a", "--all"},
            description = "Show all metrics available, which is the default. This option supersedes all other selection options."
    )
    boolean all;

    @CommandLine.Option(names = {"-g", "--gauges"},
            description = "Show all gauge metrics available.")
    boolean gauge;

    @CommandLine.Option(names = {"-c", "--counters"},
            description = "Show all counter metrics available.")
    boolean counter;

    @CommandLine.Option(names = {"-h", "--histograms"},
            description = "Show all histogram metrics available.")
    boolean histograms;

    @CommandLine.Option(names = {"-m", "--meters"},
            description = "Show all meter metrics available.")
    boolean meters;

    @CommandLine.Option(names = {"-t", "--timers"},
            description = "Show all timer metrics available.")
    boolean timers;

  }


  @CommandLine.Command(description = "Prints the metrics data.")
  public void data(MetricsDataOptions options) throws IOException, InputError {

    MetricsData metricsData = apiCall(RundeckApi::getMetricsData);

    getRdOutput().info("Displaying system metric data:");
    getRdOutput().info("Version: " + metricsData.getVersion());

    // Print all if --all is used, or if no selectors are specified.
    boolean printAll = options.isAll() || (
            !options.isGauge() &&
                    !options.isCounter() &&
                    !options.isHistograms() &&
                    !options.isMeters() &&
                    !options.isTimers()
    );

    if (printAll || options.isGauge()) {
      getRdOutput().info("Found " + metricsData.getGauges().size() + " gauge metrics.");
      if (!options.isSummary()) {
        getRdOutput().output(printMetricMap(metricsData.getGauges()));
      }
    }

    if (printAll || options.isCounter()) {
      getRdOutput().info("Found " + metricsData.getCounters().size() + " counter metrics.");
      if (!options.isSummary()) {
        getRdOutput().output(printMetricMap(metricsData.getCounters()));
      }
    }

    if (printAll || options.isHistograms()) {
      getRdOutput().info("Found " + metricsData.getHistograms().size() + " histogram metrics.");
      if (!options.isSummary()) {
        getRdOutput().output(printMetricMap(metricsData.getHistograms()));
      }
    }

    if (printAll || options.isMeters()) {
      getRdOutput().info("Found " + metricsData.getMeters().size() + " meter metrics.");
      if (!options.isSummary()) {
        getRdOutput().output(printMetricMap(metricsData.getMeters()));
      }
    }

    if (printAll || options.isTimers()) {
      getRdOutput().info("Found " + metricsData.getTimers().size() + " timer metrics.");
      if (!options.isSummary()) {
        getRdOutput().output(printMetricMap(metricsData.getTimers()));
      }
    }

  }


  /**
   * Generates a printable version of a metric map.
   */
  private static String printMetricMap(Map<String, Map<String, Object>> metricMap) {
    return metricMap.entrySet().stream()
        .map(metricEntry -> metricEntry.getValue().entrySet().stream()
            .map(metricValues -> String.format("- %s: %s",
                metricValues.getKey(),
                metricValues.getValue()
            ))
            .collect(Collectors.joining(
                "\n",
                metricEntry.getKey() + ":\n",
                "\n"
            )))
        .collect(Collectors.joining("\n"));
  }

}
