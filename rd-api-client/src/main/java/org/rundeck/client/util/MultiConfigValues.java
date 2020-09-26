package org.rundeck.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Returns first value from multiple sources
 */
public class MultiConfigValues
        implements ConfigValues
{
    private final List<ConfigValues> sources;

    public MultiConfigValues(final List<ConfigValues> sources) {
        this.sources = sources;
    }

    public MultiConfigValues(final ConfigValues... sources) {
        this.sources = new ArrayList<>(Arrays.asList(sources));
    }

    @Override
    public String get(final String key) {
        for (ConfigValues source : sources) {
            String val = source.get(key);
            if (null != val) {
                return val;
            }
        }
        return null;
    }
}
