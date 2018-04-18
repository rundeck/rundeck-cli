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

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * stream utils
 */
public class Util {
    public static long copyStream(final InputStream inputStream, final OutputStream out) throws IOException {
        return copyStream(inputStream, out, 10240);
    }

    public static long copyStream(
            final InputStream inputStream,
            final OutputStream out,
            @SuppressWarnings("SameParameterValue") final int bufferSize
    )
            throws IOException
    {
        long total = 0;
        byte[] buff = new byte[bufferSize];
        int count = inputStream.read(buff);
        while (count > 0) {
            out.write(buff, 0, count);
            total += count;
            count = inputStream.read(buff);
        }
        return total;
    }

    /**
     * Use console to prompt user for input
     *
     * @param prompt  prompt string
     * @param handler input handler, returns parsed value, or empty to prompt again
     * @param defval  default value to return if no input available or user cancels input
     * @param <T>     result type
     * @return result
     */
    public static <T> T readPrompt(String prompt, Function<String, Optional<T>> handler, T defval) {
        Console console = System.console();
        if (null == console) {
            return defval;
        }
        while (true) {
            String load = console.readLine(prompt);
            if (null == load) {
                return defval;
            }
            Optional<T> o = handler.apply(load.trim());
            if (o.isPresent()) {
                return o.get();
            }
        }
    }
}
