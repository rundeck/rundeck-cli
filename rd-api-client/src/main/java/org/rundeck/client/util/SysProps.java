package org.rundeck.client.util;

/**
 * Returns config value if it is defined in System properties,
 * it will be converted to lowercase, and "_" replaced with ".".
 */
public class SysProps
        extends ConfigBase
        implements ConfigSource
{
    public String getString(final String key, final String defval) {
        return System.getProperty(key.toLowerCase().replaceAll("_", "."), defval);
    }
}
