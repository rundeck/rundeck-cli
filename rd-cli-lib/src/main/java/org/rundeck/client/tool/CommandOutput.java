package org.rundeck.client.tool;

public interface CommandOutput {
    /**
     * Info level output, may be hidden for data/formatted output
     *
     * @param output output
     */
    void info(Object output);
    void output(Object output);

    void error(Object error);

    void warning(Object error);
}
