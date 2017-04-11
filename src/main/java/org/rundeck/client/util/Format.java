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

package org.rundeck.client.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formatting utilities
 */
public class Format {
    private Format() {
    }

    public static String format(String format, Map<?, ?> data, final String start, final String end) {
        Pattern pat = Pattern.compile(Pattern.quote(start) + "([\\w.]+)" + Pattern.quote(end));
        Matcher matcher = pat.matcher(format);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String found = matcher.group(1);
            String[] path = new String[]{found};
            if (found.contains(".")) {
                path = found.split("\\.");
            }
            Object result = descend(data, path);
            matcher.appendReplacement(sb, result != null ? result.toString() : "");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static Object descend(final Map<?, ?> data, final String... found) {
        if (found == null || found.length == 0) {
            return null;
        }
        String key = found[0];

        Object value = data.containsKey(key) ? data.get(key) : null;
        if (null == value) {
            return value;
        }
        if (found.length > 1) {
            String[] rest = new String[found.length - 1];
            System.arraycopy(found, 1, rest, 0, rest.length);
            if (value instanceof Map) {
                return descend((Map) value, rest);
            } else {
                return null;
            }
        } else {
            return value;
        }
    }

    public static Function<Map<?, ?>, String> formatter(String format, final String start, final String end) {
        return (Map<?, ?> map) -> format(format, map, start, end);
    }

    @SuppressWarnings("SameParameterValue")
    public static <X> Function<X, String> formatter(
            String format,
            Function<X, Map<?, ?>> convert,
            final String start,
            final String end
    )
    {
        return (X obj) -> format(format, convert.apply(obj), start, end);
    }

    public static String date(Date date, String simpleFormat) {
        return new SimpleDateFormat(simpleFormat).format(date);
    }

}
