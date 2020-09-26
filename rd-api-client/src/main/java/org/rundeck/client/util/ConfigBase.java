package org.rundeck.client.util;

/**
 * Base for config sources
 */
public class ConfigBase
        implements ConfigSource
{
    final ConfigValues configSource;

    public ConfigBase(final ConfigValues configSource) {
        this.configSource = configSource;
    }

    @Override
    public String get(final String key) {
        return configSource.get(key);
    }

    @Override
    public String getString(final String key, final String defval) {
        String val = get(key);
        return null == val ? defval : val;
    }

    public int getInt(final String debug, final int defval) {
        String envProp = getString(debug, null);
        if (null != envProp) {
            return Integer.parseInt(envProp);
        } else {
            return defval;
        }
    }

    public Long getLong(final String key, final Long defval) {
        String timeoutEnv = getString(key, null);
        if (null != timeoutEnv) {
            return Long.parseLong(timeoutEnv);
        } else {
            return defval;
        }
    }

    public boolean getBool(final String key, final boolean defval) {
        return "true".equalsIgnoreCase(getString(key, defval ? "true" : "false"));
    }

    public String require(final String name, final String description) throws ConfigSourceError {
        String value = get(name);
        if (null == value) {
            throw new ConfigSourceError(String.format(
                    "Environment variable %s is required: %s",
                    name,
                    description
            ));
        }
        return value;
    }
}
