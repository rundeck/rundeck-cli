package org.rundeck.client.util;

import com.simplifyops.toolbelt.ANSIColorOutput;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by greg on 7/19/16.
 */
public class Colorz {
    /**
     * Return a map with key/values replaced with colorized versions, if specified
     *
     * @param data data
     * @param key  key color, or null
     *
     * @return colorized keys/values
     */
    public static Map<?, ?> colorizeMapRecurse(
            Map<?, ?> data,
            ANSIColorOutput.Color key
    )
    {
        LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
        data.keySet().forEach(k -> {
            final Object value = data.get(k);
            result.put(
                    key != null ? ANSIColorOutput.colorize(key, k.toString()) : k,
                    value instanceof Map ? colorizeMapRecurse((Map) value, key) : value
            );
        });
        return result;
    }
}
