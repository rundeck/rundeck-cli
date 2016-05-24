package org.rundeck.client.belt;

/**
 * Created by greg on 5/24/16.
 */
public class InputError extends Exception {
    public InputError() {
    }

    public InputError(final String message) {
        super(message);
    }

    public InputError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InputError(final Throwable cause) {
        super(cause);
    }

    public InputError(
            final String message,
            final Throwable cause,
            final boolean enableSuppression,
            final boolean writableStackTrace
    )
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
