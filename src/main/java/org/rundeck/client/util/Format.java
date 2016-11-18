package org.rundeck.client.util;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by greg on 11/17/16.
 */
public class Format {
    public static String format(String format, Map<?, ?> data, final String start, final String end) {
        Pattern pat = Pattern.compile(Pattern.quote(start) + "(\\w+)" + Pattern.quote(end));
        Matcher matcher = pat.matcher(format);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String found = matcher.group(1);
            Object result = data.containsKey(found) ? data.get(found) : null;
            matcher.appendReplacement(sb, result != null ? result.toString() : "");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static Function<Map<?, ?>, String> formatter(String format, final String start, final String end) {
        return (Map<?, ?> map) -> format(format, map, start, end);
    }

    public static <X> Function<X, String> formatter(
            String format,
            Function<X, Map<?, ?>> convert,
            final String start,
            final String end
    )
    {
        return (X obj) -> format(format, convert.apply(obj), start, end);
    }
}
