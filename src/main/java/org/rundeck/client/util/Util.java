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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by greg on 5/22/16.
 */
public class Util {
    public static long copyStream(final InputStream inputStream, final OutputStream out) throws IOException {
        return copyStream(inputStream, out, 10240);
    }

    public static long copyStream(final InputStream inputStream, final OutputStream out, final int bufferSize)
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
}
