package org.rundeck.client.api.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsData {

  private String                           version;
  private Map<String, Map<String, Object>> gauges;
  private Map<String, Map<String, Object>> counters;
  private Map<String, Map<String, Object>> histograms;
  private Map<String, Map<String, Object>> meters;
  private Map<String, Map<String, Object>> timers;


  public String getVersion() {
    return version;
  }

  public MetricsData setVersion(String version) {
    this.version = version;
    return this;
  }

  public Map<String, Map<String, Object>> getGauges() {
    return gauges;
  }

  public MetricsData setGauges(Map<String, Map<String, Object>> gauges) {
    this.gauges = gauges;
    return this;
  }

  public Map<String, Map<String, Object>> getCounters() {
    return counters;
  }

  public MetricsData setCounters(Map<String, Map<String, Object>> counters) {
    this.counters = counters;
    return this;
  }

  public Map<String, Map<String, Object>> getHistograms() {
    return histograms;
  }

  public MetricsData setHistograms(Map<String, Map<String, Object>> histograms) {
    this.histograms = histograms;
    return this;
  }

  public Map<String, Map<String, Object>> getMeters() {
    return meters;
  }

  public MetricsData setMeters(Map<String, Map<String, Object>> meters) {
    this.meters = meters;
    return this;
  }

  public Map<String, Map<String, Object>> getTimers() {
    return timers;
  }

  public MetricsData setTimers(Map<String, Map<String, Object>> timers) {
    this.timers = timers;
    return this;
  }

  @Override
  public String toString() {
    return "MetricsData{" +
        "version='" + version + '\'' +
        ", gauges=" + gauges +
        ", counters=" + counters +
        ", histograms=" + histograms +
        ", meters=" + meters +
        ", timers=" + timers +
        '}';
  }
}
