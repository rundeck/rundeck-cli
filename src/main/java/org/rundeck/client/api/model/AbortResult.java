package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 5/20/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbortResult {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reason {
        public String status;
        public String reason;
    }

    public Reason abort;
    public Execution execution;
}
