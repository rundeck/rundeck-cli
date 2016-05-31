package org.rundeck.util.toolbelt;

/**
 * Indicates input parsing error occurd
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
