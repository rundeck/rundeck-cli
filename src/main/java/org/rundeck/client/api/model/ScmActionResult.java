package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Result of <a href="http://rundeck.org/docs/api/index.html#setup-scm-plugin-for-a-project">Setup SCM Plugin for a
 * Project</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmActionResult {
    public String message;
    public String nextAction;
    public boolean success;
    public Map<String, String> validationErrors;

    public Map<?, ?> toMap() {
        return validationErrors;
    }
}
