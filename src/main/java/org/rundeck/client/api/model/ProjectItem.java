package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Created by greg on 5/19/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

    public String toBasicString() {
        return String.format(
                "%s%s",
                name,
                description != null && !"".equals(description.trim()) ? ": " + description : ""
        );
    }

}
