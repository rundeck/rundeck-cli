package org.rundeck.client.api.model;

/**
 * Created by greg on 7/19/16.
 */
public interface ErrorDetail {
    String getErrorCode();

    String getErrorMessage();

    int getApiVersion();

    public default String toCodeString() {
        if (null != getErrorCode()) {
            return String.format(
                    "[code: %s; APIv%d]",
                    getErrorCode(),
                    getApiVersion()
            );
        }
        return "";
    }

}
