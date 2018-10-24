package org.rundeck.client.tool.commands.pro;

import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.toolbelt.HasSubCommands;

import java.util.Arrays;
import java.util.List;

public class ProCommands
        extends AppCommand
        implements HasSubCommands
{
    public ProCommands(final RdApp rdApp) {
        super(rdApp);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(
                new Reactions(getRdApp()),
                new Subscriptions(getRdApp())
        );
    }
}
