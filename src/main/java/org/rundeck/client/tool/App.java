package org.rundeck.client.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by greg on 3/28/16.
 */
public class App {
    public static void main(String[] args) throws IOException {
        if (args[0].equals("jobs")) {
            List<String> strings = new ArrayList<>(Arrays.asList(args));
            strings.remove(0);
            Jobs.main(strings.toArray(new String[strings.size()]));
        }
    }
}
