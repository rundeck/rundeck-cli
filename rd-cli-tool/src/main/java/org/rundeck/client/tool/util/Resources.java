package org.rundeck.client.tool.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Holds collection of closeable resources
 */
public class Resources
        implements Closeable
{
    private final Collection<Closeable> closeableResources = new ArrayList<>();

    public <T extends Closeable> T add(T closeable) {
        closeableResources.add(closeable);
        return closeable;
    }

    @Override
    public void close() throws IOException {
        closeableResources.forEach(
                closeable -> {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        closeableResources.clear();
    }
}
