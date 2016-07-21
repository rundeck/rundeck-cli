package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Result of <a href="http://rundeck.org/docs/api/index.html#get-project-scm-config">Get Project SCM Config</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmConfig {
    public boolean enabled;
    public String integration;
    public String project;
    public String type;
    public Map<String, String> config;
}
