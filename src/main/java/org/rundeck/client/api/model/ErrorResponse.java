package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 5/22/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    public String error;
    public int apiversion;
    public String errorCode;
    public String message;

    public String toCodeString() {
        if (null != errorCode) {
            return String.format(
                    "[code: %s; APIv%d]",
                    errorCode,
                    apiversion
            );
        }
        return "";
    }

    @Override
    public String toString() {
        return String.format(
                "%s%n%s%n",
                message != null ? message : error != null ? error : "(no message)",
                toCodeString()
        );
    }
}
