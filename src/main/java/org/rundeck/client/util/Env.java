package org.rundeck.client.util;

import com.simplifyops.toolbelt.InputError;

/**
 * Created by greg on 11/17/16.
 */
public class Env implements ConfigSource {

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

    public String getString(final String key, final String defval) {
        String val = System.getenv(key);
        if (val != null) {
            return val;
        } else {
            return defval;
        }
    }

    @Override
    public String get(final String key) {
        return getString(key, null);
    }

    public String require(final String name, final String description) throws InputError {
        String value = System.getenv(name);
        if (null == value) {
            throw new InputError(String.format(
                    "Environment variable %s is required: %s",
                    name,
                    description
            ));
        }
        return value;
    }

}
