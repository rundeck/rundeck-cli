package org.rundeck.client.util;

import java.util.Map;

/**
 * Provides config values from a map
 */
public class MapConfigValues
        implements ConfigValues
{
    private final Map<String, String> values;

    public MapConfigValues(final Map<String, String> values) {
        this.values = values;
    }

    @Override
    public String get(final String key) {
        return values.get(key);
    }
}
