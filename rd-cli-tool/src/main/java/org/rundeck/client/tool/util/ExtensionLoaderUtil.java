package org.rundeck.client.tool.util;

import org.rundeck.client.tool.extension.RdCommandExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ExtensionLoaderUtil {
    private static ServiceLoader<RdCommandExtension>
            extensionServiceLoader =
            ServiceLoader.load(RdCommandExtension.class);

    public static List<RdCommandExtension> list() {
        List<RdCommandExtension> list = new ArrayList<>();
        for (RdCommandExtension rdCommandExtension : extensionServiceLoader) {
            list.add(rdCommandExtension);
        }
        return list;
    }
}
