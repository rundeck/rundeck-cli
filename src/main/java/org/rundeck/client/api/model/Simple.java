package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author greg
 * @since 4/4/17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Simple {
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
