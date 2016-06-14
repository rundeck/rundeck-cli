package org.rundeck.client.api.model.sysinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by greg on 6/13/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemStats {
    private Map<String, Object> timestamp;
    private Map<String, Object> rundeck;
    private Map<String, Object> executions;
    private Map<String, Object> os;
    private Map<String, Object> jvm;
    private Map<String, Map> stats;
    private Link metrics;
    private Link threadDump;
    private Link healthcheck;

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", timestamp);
        data.put("rundeck", rundeck);
        data.put("executions", executions);
        data.put("os", os);
        data.put("jvm", jvm);
        data.put("stats", stats);
        data.put("metrics", metrics.toMap());
        data.put("threadDump", threadDump.toMap());
        data.put("healthcheck", healthcheck.toMap());
        return data;
    }

    public Map<String, Object> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Map<String, Object> timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getRundeck() {
        return rundeck;
    }

    public void setRundeck(Map<String, Object> rundeck) {
        this.rundeck = rundeck;
    }

    public Map<String, Object> getExecutions() {
        return executions;
    }

    public void setExecutions(Map<String, Object> executions) {
        this.executions = executions;
    }

    public Map<String, Object> getOs() {
        return os;
    }

    public void setOs(Map<String, Object> os) {
        this.os = os;
    }

    public Map<String, Object> getJvm() {
        return jvm;
    }

    public void setJvm(Map<String, Object> jvm) {
        this.jvm = jvm;
    }

    public Map<String, Map> getStats() {
        return stats;
    }

    public void setStats(Map<String, Map> stats) {
        this.stats = stats;
    }

    public Link getMetrics() {
        return metrics;
    }

    public void setMetrics(Link metrics) {
        this.metrics = metrics;
    }

    public Link getThreadDump() {
        return threadDump;
    }

    public void setThreadDump(Link threadDump) {
        this.threadDump = threadDump;
    }

    public Link getHealthcheck() {
        return healthcheck;
    }

    public void setHealthcheck(Link healthcheck) {
        this.healthcheck = healthcheck;
    }

    @Override
    public String toString() {
        return "{" + "\n" +
               "timestamp=" + timestamp + "\n" +
               ", rundeck=" + rundeck + "\n" +
               ", executions=" + executions + "\n" +
               ", os=" + os + "\n" +
               ", jvm=" + jvm + "\n" +
               ", stats=" + stats + "\n" +
               ", metrics=" + metrics + "\n" +
               ", threadDump=" + threadDump + "\n" +
               ", healthcheck=" + healthcheck + "\n" +
               '}';
    }
}
