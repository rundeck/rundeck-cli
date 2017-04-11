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
