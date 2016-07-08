package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by greg on 7/8/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduledJobItem extends JobItem {
    private String serverNodeUUID;
    private boolean scheduled;
    private boolean scheduleEnabled;
    private boolean enabled;
    private boolean serverOwner;


    public String getServerNodeUUID() {
        return serverNodeUUID;
    }

    public void setServerNodeUUID(String serverNodeUUID) {
        this.serverNodeUUID = serverNodeUUID;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public boolean isScheduleEnabled() {
        return scheduleEnabled;
    }

    public void setScheduleEnabled(boolean scheduleEnabled) {
        this.scheduleEnabled = scheduleEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isServerOwner() {
        return serverOwner;
    }

    public void setServerOwner(boolean serverOwner) {
        this.serverOwner = serverOwner;
    }

    public Map<Object, Object> toMap() {
        HashMap<Object, Object> map = new LinkedHashMap<>();
        map.put("job", toBasicString());
        map.put("serverNodeUUID", getServerNodeUUID());
        map.put("scheduled", scheduled);
        map.put("scheduleEnabled", scheduleEnabled);
        map.put("enabled", enabled);
        map.put("serverOwner", serverOwner);
        return map;
    }

    @Override
    public String toString() {
        return super.toBasicString() +
               "\n  serverNodeUUID='" + serverNodeUUID + '\'' +
               ", scheduled=" + scheduled +
               ", scheduleEnabled=" + scheduleEnabled +
               ", enabled=" + enabled +
               ", serverOwner=" + serverOwner;

    }
}
