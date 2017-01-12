package org.rundeck.client.tool;

import org.rundeck.client.util.ConfigSource;

/**
 * @author greg
 * @since 1/11/17
 */
public interface AppConfig extends ConfigSource {
    boolean isAnsiEnabled();

    String getDateFormat();
}
