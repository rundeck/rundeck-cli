package org.rundeck.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Returns first value from multiple sources
 */
public class MultiConfigSource
        extends ConfigBase
        implements ConfigSource
{
    private final List<ConfigSource> sources;

    public MultiConfigSource(final List<ConfigSource> sources) {
        this.sources = sources;
    }

    public MultiConfigSource(final ConfigSource... sources) {
        this.sources = new ArrayList<>(Arrays.asList(sources));
    }

    @Override
    public String getString(final String key, final String defval) {
        for (ConfigSource source : sources) {
            String val = source.get(key);
            if (null != val) {
                return val;
            }
        }
        return defval;
    }
}
