package org.rundeck.client.util;

import java.util.List;

/**
 * String quoting for rundeck
 */
public class Quoting {

    public static final String DQ = "\"";
    public static final String Q = "'";
    public static final String DDQ = "\"\"";

    public static String joinStringQuoted(final List<String> commandString) {
        if (null == commandString) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : commandString) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (needsQuoting(s)) {
                quoteString(sb, s);
            } else {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static boolean needsQuoting(final String s) {
        return (s.contains(" ") || s.contains(DQ) || s.contains(Q))
               &&
               !(
                       (s.startsWith(Q) && s.endsWith(Q))
                       ||
                       (s.startsWith(DQ) && s.endsWith(DQ))
               );
    }

    public static StringBuilder quoteString(final StringBuilder sb, final String s) {
        return sb.append(DQ).append(s.replaceAll(DQ, DDQ)).append(DQ);
    }

    public static String quoteString(final String s) {
        StringBuilder sb = new StringBuilder();
        return quoteString(sb, s).toString();
    }
}
