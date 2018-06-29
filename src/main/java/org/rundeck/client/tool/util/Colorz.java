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

package org.rundeck.client.tool.util;

import org.rundeck.toolbelt.ANSIColorOutput;

import java.util.LinkedHashMap;
import java.util.Map;

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
