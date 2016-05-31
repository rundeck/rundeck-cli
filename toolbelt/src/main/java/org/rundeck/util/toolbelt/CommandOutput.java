package org.rundeck.util.toolbelt;

import java.io.IOException;

/**
 * Interface for output
 */
public interface CommandOutput {
    void output(Object output);

    void error(Object error);

    void warning(Object error);

}
