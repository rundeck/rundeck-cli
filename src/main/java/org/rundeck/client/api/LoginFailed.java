package org.rundeck.client.api;

/**
 * @author greg
 * @since 5/24/17
 */
public class LoginFailed extends RuntimeException {
    public LoginFailed() {
    }

    public LoginFailed(final String message) {
        super(message);
    }

    public LoginFailed(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LoginFailed(final Throwable cause) {
        super(cause);
    }

    public LoginFailed(
            final String message,
            final Throwable cause,
            final boolean enableSuppression,
            final boolean writableStackTrace
    )
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
