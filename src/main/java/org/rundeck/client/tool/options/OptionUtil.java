package org.rundeck.client.tool.options;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.util.ConfigSource;
import org.rundeck.client.util.Env;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author greg
 * @since 11/28/16
 */
public class OptionUtil {

    /**
     * Parse a list of "--key=value" into a map
     *
     * @param input
     *
     * @return
     *
     * @throws InputError
     */
    public static Map<String, String> parseKeyValueMap(final List<String> input) throws InputError {
        return parseKeyValueMap(input, "--", "=");
    }

    /**
     * Parse a list of "{prefix}key${delim}value" into a map, using specified delimiter and prefix
     *
     * @param input
     * @param keyPrefix
     * @param delim
     *
     * @return
     *
     * @throws InputError
     */
    public static Map<String, String> parseKeyValueMap(
            final List<String> input,
            final String keyPrefix,
            final String delim
    ) throws InputError
    {
        Map<String, String> config = new HashMap<>();
        if (input.size() > 0) {
            for (String s : input) {
                if (keyPrefix != null && !s.startsWith(keyPrefix)) {
                    throw new InputError(String.format("Expected %skey%svalue, but saw: %s", keyPrefix, delim, s));
                } else if (keyPrefix != null) {
                    s = s.substring(keyPrefix.length());
                }
                String[] arr = s.split(Pattern.quote(delim), 2);
                if (arr.length != 2) {
                    throw new InputError(String.format("Expected %skey%svalue, but saw: %s", keyPrefix, delim, s));
                }
                config.put(arr[0], arr[1]);
            }
        }
        return config;
    }
}
