package org.rundeck.client.tool.commands.enterprise.cluster;

import org.rundeck.client.tool.commands.enterprise.BaseExtension;
import picocli.CommandLine;

@CommandLine.Command(
        name = "cluster",
        description = "Manage Rundeck Enterprise Cluster",
        subcommands = {
                Mode.class
        }
)
public class Cluster extends BaseExtension {

}
