package org.rundeck.client.tool;

/**
 * Created by greg on 5/20/16.
 */
public class AuthorizationFailed extends RequestFailed {
    public AuthorizationFailed(final int statusCode, final String status) {
        super(statusCode, status);
    }

    public AuthorizationFailed(final String message, final int statusCode, final String status) {
        super(message, statusCode, status);
    }

    public AuthorizationFailed(
            final String message,
            final Throwable cause,
            final int statusCode,
            final String status
    )
    {
        super(message, cause, statusCode, status);
    }

    public AuthorizationFailed(final Throwable cause, final int statusCode, final String status) {
        super(cause, statusCode, status);
    }
}
