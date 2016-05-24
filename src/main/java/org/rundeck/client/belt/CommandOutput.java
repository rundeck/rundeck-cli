package org.rundeck.client.belt;

import java.io.IOException;

/**
 * Created by greg on 5/23/16.
 */
public interface CommandOutput {
    void output(Object output);

    void error(Object error);

    void warning(Object error);

}
