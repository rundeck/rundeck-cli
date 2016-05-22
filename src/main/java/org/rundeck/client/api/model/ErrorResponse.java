package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 5/22/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    public boolean error;
    public int apiversion;
    public String errorCode;
    public String message;

    @Override
    public String toString() {
        return String.format(
                "%s%n(APIv%d, code: %s)%n",
                message,
                apiversion,
                errorCode
        );
    }
}
