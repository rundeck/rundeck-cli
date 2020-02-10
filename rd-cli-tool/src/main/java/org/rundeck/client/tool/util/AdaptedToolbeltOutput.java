package org.rundeck.client.tool.util;

import org.rundeck.client.tool.CommandOutput;

public class AdaptedToolbeltOutput
        implements CommandOutput
{
    private final org.rundeck.toolbelt.CommandOutput commandOutput;

    public AdaptedToolbeltOutput(final org.rundeck.toolbelt.CommandOutput commandOutput) {
        this.commandOutput = commandOutput;
    }

    @Override
    public void info(final Object output) {
        commandOutput.info(output);
    }

    @Override
    public void output(final Object output) {
        commandOutput.output(output);
    }

    @Override
    public void error(final Object error) {
        commandOutput.error(error);
    }

    @Override
    public void warning(final Object error) {
        commandOutput.warning(error);
    }
}
