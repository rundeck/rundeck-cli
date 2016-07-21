package org.rundeck.client.util;

import com.simplifyops.toolbelt.ANSIColorOutput;

import java.util.Arrays;
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
     * @param colors  key color, or null
     *
     * @return colorized keys/values
     */
    public static Map<?, ?> colorizeMapRecurse(
            Map<?, ?> data,
            ANSIColorOutput.Color... colors
    )
    {
        LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
        data.keySet().forEach(k -> {
            final Object value = data.get(k);
            ANSIColorOutput.Color keyColor = colors != null && colors.length > 0 ? colors[0] : null;
            ANSIColorOutput.Color[] subcolors = null;
            if (colors != null && colors.length > 1) {
                subcolors = new ANSIColorOutput.Color[colors.length - 1];
                System.arraycopy(colors, 1, subcolors, 0, subcolors.length);
            }
            result.put(
                    keyColor != null
                    ? ANSIColorOutput.colorize(
                            keyColor,
                            k.toString()
                    )
                    : k,

                    value instanceof Map && subcolors != null
                    ? colorizeMapRecurse(
                            (Map) value,
                            subcolors
                    )

                    : value
            );
        });
        return result;
    }
}
