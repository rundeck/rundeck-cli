package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
public interface AdhocBaseOptions extends ProjectNameOptions, FollowOptions {


    @Option(shortName = "C",
            longName = "threadcount",
            description = "Execute using COUNT threads",
            defaultValue = {"1"})
    int getThreadcount();

    boolean isThreadcount();

    @Option(shortName = "K",
            longName = "keepgoing",
            description = "Keep going when an error occurs")
    boolean isKeepgoing();

    @Option(shortName = "F", longName = "filter", description = "A node filter string")
    String getFilter();

    boolean isFilter();


    @Option(shortName = "s", longName = "script", description = "Dispatch specified script file")
    File getScriptFile();

    boolean isScriptFile();


    @Option(shortName = "u", longName = "url", description = "Download a URL and dispatch it as a script")
    URL getUrl();

    boolean isUrl();

    @Option(shortName = "S", longName = "stdin", description = "Execute input read from STDIN")
    boolean isStdin();

    @Unparsed(name = "-- COMMAND", description = "Dispatch specified command string")
    List<String> getCommandString();


}
