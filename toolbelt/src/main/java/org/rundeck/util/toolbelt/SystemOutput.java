package org.rundeck.util.toolbelt;

/**
 * Output to system out/err
 */
public class SystemOutput implements CommandOutput {
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
