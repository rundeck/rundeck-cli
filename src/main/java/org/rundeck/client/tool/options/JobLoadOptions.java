package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

/**
 * Created by greg on 5/20/16.
 */
public interface JobLoadOptions extends JobBaseOptions {

    @Option(shortName = "d", longName = "duplicate", defaultValue = "update", pattern = "^(update|skip|create)$")
    String getDuplicate();

    @Option(shortName = "r", longName = "remove-uuids")
    boolean isRemoveUuids();

}
