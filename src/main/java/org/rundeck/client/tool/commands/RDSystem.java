package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.ANSIColorOutput;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.HasSubCommands;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.tool.commands.system.ACLs;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Colorz;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by greg on 6/13/16.
 */
@Command(description = "View system information", value = "system")
public class RDSystem extends ApiCommand implements HasSubCommands {
    public RDSystem(final Client<RundeckApi> client) {
        super(client);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(
                new ACLs(client)
        );
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
