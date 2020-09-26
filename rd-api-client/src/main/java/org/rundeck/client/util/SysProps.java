package org.rundeck.client.util;

/**
 * Returns config value if it is defined in System properties, it will be converted to lowercase, and "_" replaced with
 * ".".
 */
public class SysProps
        implements ConfigValues
{
    private GetProperty getter = System::getProperty;

    public void setGetter(GetProperty getter) {
        this.getter = getter;
    }

    static interface GetProperty {
        public String getProperty(String key, String defval);
    }

    public String get(final String key) {
        return getter.getProperty(key.toLowerCase().replaceAll("_", "."), null);
    }
}
