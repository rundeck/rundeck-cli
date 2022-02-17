package org.rundeck.client.tool.commands.enterprise.license;

import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi;
import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.util.ServiceClient;

public class BaseExtension
        implements RdCommandExtension
{

    @Getter @Setter protected RdTool rdTool;

    protected ServiceClient<EnterpriseApi> getClient() throws InputError {
        return rdTool.getRdApp().getClient(EnterpriseApi.class);
    }

    protected CommandOutput getOutput() {
        return rdTool.getRdApp().getOutput();
    }
}
