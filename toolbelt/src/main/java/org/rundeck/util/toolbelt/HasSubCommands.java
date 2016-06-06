package org.rundeck.util.toolbelt;

import java.util.List;

/**
 * Indicates that an object being evaluated as a Command container,
 * has other sub command objects to be evaluated.
 */
public interface HasSubCommands {
    /**
     * @return list of subcommand objects
     */
    public List<Object> getSubCommands();
}
