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
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by greg on 5/19/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectItem {

    private String name;
    private String description;
    private String url;
    private Map<String, String> config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public Map<Object, Object> toBasicMap() {

        HashMap<Object, Object> detail = new LinkedHashMap<>();
        detail.put("name", getName());
        detail.put("description", description != null && !"".equals(description.trim()) ? description : "");
        detail.put("url", getUrl());
        return detail;
    }
    public Map<Object, Object> toMap() {

        HashMap<Object, Object> detail = new LinkedHashMap<>(toBasicMap());
        if (null != getConfig()) {
            detail.put("config", getConfig());
        }
        return detail;
    }
    public String toBasicString() {
        return String.format(
                "%s%s",
                name,
                description != null && !"".equals(description.trim()) ? ": " + description : ""
        );
    }

}
