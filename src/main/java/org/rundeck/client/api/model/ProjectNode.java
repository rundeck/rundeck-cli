package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.*;

import java.util.Map;

/**
 * @author greg
 * @since 11/22/16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectNode {
    private Map<String, String> attributes;

    @JsonCreator
    public ProjectNode(final Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return attributes.get("nodename");
    }
    @JsonValue()
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "org.rundeck.client.api.model.ProjectNode{" +
               "attributes=" + attributes +
               '}';
    }
}
