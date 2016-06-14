package org.rundeck.client.tool.commands;

import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.util.Client;
import org.rundeck.util.toolbelt.Command;
import org.rundeck.util.toolbelt.CommandOutput;

import java.io.IOException;

/**
 * Created by greg on 6/13/16.
 */
@Command(description = "View system information", value = "system")
public class RDSystem extends ApiCommand {
    public RDSystem(final Client<RundeckApi> client) {
        super(client);
    }


    /**
     * Read system info
     */
    @Command(description = "Print system information and stats.")
    public void info(CommandOutput output) throws IOException {
        SystemInfo systemInfo = client.checkError(client.getService().systemInfo());
        output.output(systemInfo.system.toMap());
    }

}
