package org.rundeck.client.tool.output;

import org.rundeck.client.tool.CommandOutput;

public class SystemOutput implements CommandOutput {
    @Override
    public void info(final Object output) {
        System.out.println(output);
    }

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

    public void outPrint(final Object out) {
        System.out.print(out);
    }

    public void errorPrint(final Object error) {
        System.err.print(error);
    }

}