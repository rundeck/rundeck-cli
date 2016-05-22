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
