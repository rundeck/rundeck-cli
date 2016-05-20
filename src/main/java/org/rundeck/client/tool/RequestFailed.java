package org.rundeck.client.tool;

/**
 * Created by greg on 5/20/16.
 */
public class RequestFailed extends RuntimeException {
    int statusCode;
    String status;

    public RequestFailed(final int statusCode, final String status) {
        this.statusCode = statusCode;
        this.status = status;
    }

    public RequestFailed(final String message, final int statusCode, final String status) {
        super(message);
        this.statusCode = statusCode;
        this.status = status;
    }

    public RequestFailed(final String message, final Throwable cause, final int statusCode, final String status) {
        super(message, cause);
        this.statusCode = statusCode;
        this.status = status;
    }

    public RequestFailed(final Throwable cause, final int statusCode, final String status) {
        super(cause);
        this.statusCode = statusCode;
        this.status = status;
    }
}
