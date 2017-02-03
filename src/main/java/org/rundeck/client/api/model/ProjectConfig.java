package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

/**
 * input/output from Project Config API endpoints
 *
 * @author greg
 * @since 2/2/17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectConfig {
    private Map<String, String> config;

    @JsonCreator
    public ProjectConfig(final Map<String, String> config) {
        this.config = config;
    }

    @JsonValue()
    public Map<String, String> getConfig() {
        return config;
    }
}
