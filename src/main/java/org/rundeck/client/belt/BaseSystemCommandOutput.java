package org.rundeck.client.belt;

/**
 * Created by greg on 5/23/16.
 */
public class BaseSystemCommandOutput implements CommandOutput {
    @Override
    public void output(final Object output) {
        System.out.println(output);
    }

    @Override
    public void error(final Object error) {
        System.err.println(error);
    }

    @Override
    public void warning(final Object error) {
        System.err.println(error);
    }

}
