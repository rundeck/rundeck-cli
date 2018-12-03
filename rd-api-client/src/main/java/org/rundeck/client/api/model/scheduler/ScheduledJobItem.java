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
import org.rundeck.client.api.model.JobItem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduledJobItem extends JobItem {
    private String serverNodeUUID;
    private boolean scheduled;
    private boolean scheduleEnabled;
    private boolean enabled;
    private Boolean serverOwner;


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

    public Boolean isServerOwner() {
        return serverOwner;
    }

    public void setServerOwner(Boolean serverOwner) {
        this.serverOwner = serverOwner;
    }

    public Map<Object, Object> toMap() {
        HashMap<Object, Object> map = new LinkedHashMap<>();


        HashMap<Object, Object> detail = new LinkedHashMap<>(super.toMap());
        detail.put("scheduled", scheduled);
        detail.put("scheduleEnabled", scheduleEnabled);
        detail.put("enabled", enabled);


        if (null != serverOwner && null != getServerNodeUUID()) {
            HashMap<Object, Object> schedule = new LinkedHashMap<>();
            schedule.put("serverNodeUUID", getServerNodeUUID());
            schedule.put("serverOwner", serverOwner);
            detail.put("scheduler", schedule);
        }
        return detail;
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
