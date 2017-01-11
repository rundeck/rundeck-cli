package org.rundeck.client.util;

import com.simplifyops.toolbelt.InputError;

/**
 * @author greg
 * @since 1/11/17
 */
public interface ConfigSource {

    public int getInt(final String key, final int defval);

    public Long getLong(final String key, final Long defval);

    public boolean getBool(final String key, final boolean defval);

    public String getString(final String key, final String defval);

    public String get(final String key);

    public String require(final String key, final String description) throws InputError;

}
