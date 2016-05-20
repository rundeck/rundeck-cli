package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 5/20/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteJob {
    private String error;
    private String message;
    private String id;
    private String errorCode;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String toBasicString() {
        if (null != getErrorCode()) {
            return String.format("[%s] (%s) %s", getId(), getErrorCode(), getError() != null ? getError() : "");
        } else if (null != getMessage()) {
            return String.format("[%s] %s", getId(), getMessage());
        } else {
            return String.format("[%s]", getId());
        }
    }
}
