package org.rundeck.util.toolbelt;

/**
 * Created by greg on 6/13/16.
 */
public class CommandWarning extends CommandRunFailure {
    public CommandWarning() {
    }

    public CommandWarning(final String message) {
        super(message);
    }

    public CommandWarning(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CommandWarning(final Throwable cause) {
        super(cause);
    }

    public CommandWarning(
            final String message,
            final Throwable cause,
            final boolean enableSuppression,
            final boolean writableStackTrace
    )
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
