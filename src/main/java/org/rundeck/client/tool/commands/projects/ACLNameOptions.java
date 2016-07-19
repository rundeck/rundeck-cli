package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by greg on 7/19/16.
 */
public interface ACLNameOptions {
    @Option(shortName = "n", longName = "name", description = "name of the aclpolicy file")
    String getName();
}
