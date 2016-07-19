package org.rundeck.client.tool.commands.projects;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 7/19/16.
 */
public interface ACLFileOptions {
    @Option(shortName = "f", longName = "file", description = "ACLPolicy file to upload")
    File getFile();
}
