/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.tool.options;


import org.rundeck.client.tool.InputError;

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
     */
    public static Map<String, String> parseKeyValueMap(final List<String> input) throws InputError {
        return parseKeyValueMap(input, "--", "=");
    }

    /**
     * Parse a list of "{prefix}key${delim}value" into a map, using specified delimiter and prefix
     *
     * @param keyPrefix prefix
     * @param delim delimiter
     *
     */
    @SuppressWarnings("SameParameterValue")
    public static Map<String, String> parseKeyValueMap(
            final List<String> input,
            final String keyPrefix,
            final String delim
    ) throws InputError
    {
        Map<String, String> config = new HashMap<>();
        if (!input.isEmpty()) {
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
