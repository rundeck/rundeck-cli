package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.HasSubCommands;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.tool.commands.system.ACLs;
import org.rundeck.client.util.Client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by greg on 6/13/16.
 */
@Command(description = "View system information", value = "system")
public class RDSystem extends ApiCommand implements HasSubCommands {
    public RDSystem(final HasClient client) {
        super(client);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(
                new ACLs(this::getClient)
        );
    }

    /**
     * Read system info
     */
    @Command(description = "Print system information and stats.")
    public void info(CommandOutput output) throws IOException, InputError {
        SystemInfo systemInfo = apiCall(RundeckApi::systemInfo);
        output.output(systemInfo.system.toMap());
    }

}
