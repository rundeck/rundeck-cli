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

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.metrics.EndpointListResult;
import org.rundeck.client.api.model.metrics.HealthCheckStatus;
import org.rundeck.client.api.model.metrics.MetricsData;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.InputError;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * scheduler subcommands
 */
@Command(description = "View metrics endpoints information.")
public class Metrics extends AppCommand {
  public Metrics(final RdApp client) {
    super(client);
  }


  // rd metrics list

  @CommandLineInterface(application = "list")
  interface EndpointListOptions extends VerboseOption {
  }

  @Command(description = "Print system information and stats.")
  public void list(EndpointListOptions options, CommandOutput output) throws IOException, InputError {

    EndpointListResult endpointList = apiCall(RundeckApi::listMetricsEndpoints);

    if (endpointList.size() > 0) {
      output.info(endpointList.size() + " metric endpoints:");
      output.output(endpointList.getEndpointLinks().entrySet().stream()
          .map(metricEntry -> metricEntry.getKey() +
              (options.isVerbose() ? (" " + metricEntry.getValue().getHref()) : ""))
          .collect(Collectors.joining("\n", "", "\n"))
      );
    }
    else {
      output.warning("No metrics endpoints found.");
    }
  }


  // rd metrics healthcheck

  @CommandLineInterface(application = "healthcheck")
  interface HealthCheckOptions {
    @Option(shortName = "u",
        longName = "unhealthy",
        description = "Show only checks with unhealthy status.")
    boolean isUnhealthyOnly();

    @Option(shortName = "f",
        longName = "fail",
        description = "Exit with unsuccessful status if unhealthy checks are found.")
    boolean failOnUnhealthy();

  }

  @Command(description = "Print health check status information.")
  public boolean healthcheck(HealthCheckOptions options, CommandOutput output) throws IOException, InputError {

    Map<String, HealthCheckStatus> healthCheckStatus = apiCall(RundeckApi::getHealthCheckMetrics);

    // Obtain unhealthy list.
    Map<String, HealthCheckStatus> unhealthyList = healthCheckStatus.entrySet().stream()
        .filter(entry -> !entry.getValue().isHealthy())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue));

    Map<String, HealthCheckStatus> statusList = options.isUnhealthyOnly() ? unhealthyList : healthCheckStatus;

    if (statusList.size() > 0) {
      output.info("Showing current health status for " + statusList.size() + " checks:");

      output.output(statusList.entrySet().stream()
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
    }
    else {
      output.warning("No results found.");
    }

    return !options.failOnUnhealthy() || unhealthyList.size() == 0;
  }

  // rd metrics threads

  @CommandLineInterface(application = "threads")
  interface ThreadsOptions extends VerboseOption {
//    @Option(shortName = "u",
//        longName = "unhealthy",
//        description = "Show only checks with unhealthy status.")
//    boolean isUnhealthyOnly();

  }

  @Command(description = "Print system threads status information.")
  public void threads(ThreadsOptions options, CommandOutput output) throws IOException, InputError {

    ResponseBody response = apiCall(RundeckApi::getThreadMetrics);

    output.info("System threads status: ");
    if (options.isVerbose()) {
      output.output(response.string());
    }
    else {
      output.output(Stream.of(response.string().split("\n"))
          .filter(s -> s.matches("^\\S+.*"))
          .collect(Collectors.joining("\n")));
    }
  }

  // rd metrics ping

//  @CommandLineInterface(application = "ping")
//  interface PingOptions {
//
//  }

  @Command(description = "Returns a simple response.")
  public void ping(CommandOutput output) throws IOException, InputError {

    output.info(printTimestamp() + "Pinging server...");
    ResponseBody response = apiCall(RundeckApi::getPing);
    output.info(printTimestamp() + response.string());
  }

  /**
   * @return A formatted string with the current local time. Such as "[2007-12-03T10:15:30] ".
   */
  private String printTimestamp() {
    return "[" + LocalDateTime.now().toString() + "] ";
  }


  // rd metrics data

  @CommandLineInterface(application = "threads")
  interface MetricsDataOptions {

    @Option(shortName = "s",
        longName = "summary",
        description = "Show only a summary of metric data selected."
    )
    boolean isSummary();

    @Option(shortName = "a",
        longName = "all",
        description = "Show all metrics available, which is the default. This option supersedes all other selection options."
    )
    boolean isAll();

    @Option(shortName = "g",
        longName = "gauges",
        description = "Show all gauge metrics available.")
    boolean isGauge();

    @Option(shortName = "c",
        longName = "counters",
        description = "Show all counter metrics available.")
    boolean isCounter();

    @Option(shortName = "h",
        longName = "histograms",
        description = "Show all histogram metrics available.")
    boolean isHistograms();

    @Option(shortName = "m",
        longName = "meters",
        description = "Show all meter metrics available.")
    boolean isMeters();

    @Option(shortName = "t",
        longName = "timers",
        description = "Show all timer metrics available.")
    boolean isTimers();

  }


  @Command(description = "Prints the metrics data.")
  public void data(MetricsDataOptions options, CommandOutput output) throws IOException, InputError {

    MetricsData metricsData = apiCall(RundeckApi::getMetricsData);

    output.info("Displaying system metric data:");
    output.info("Version: " + metricsData.getVersion());

    // Print all if --all is used, or if no selectors are specified.
    boolean printAll = options.isAll() || (
        !options.isGauge() &&
            !options.isCounter() &&
            !options.isHistograms() &&
            !options.isMeters() &&
            !options.isTimers()
    );

    if (printAll || options.isGauge()) {
      output.info("Found " + metricsData.getGauges().size() + " gauge metrics.");
      if (!options.isSummary()) {
        output.output(printMetricMap(metricsData.getGauges()));
      }
    }

    if (printAll || options.isCounter()) {
      output.info("Found " + metricsData.getCounters().size() + " counter metrics.");
      if (!options.isSummary()) {
        output.output(printMetricMap(metricsData.getCounters()));
      }
    }

    if (printAll || options.isHistograms()) {
      output.info("Found " + metricsData.getHistograms().size() + " histogram metrics.");
      if (!options.isSummary()) {
        output.output(printMetricMap(metricsData.getHistograms()));
      }
    }

    if (printAll || options.isMeters()) {
      output.info("Found " + metricsData.getMeters().size() + " meter metrics.");
      if (!options.isSummary()) {
        output.output(printMetricMap(metricsData.getMeters()));
      }
    }

    if (printAll || options.isTimers()) {
      output.info("Found " + metricsData.getTimers().size() + " timer metrics.");
      if (!options.isSummary()) {
        output.output(printMetricMap(metricsData.getTimers()));
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
