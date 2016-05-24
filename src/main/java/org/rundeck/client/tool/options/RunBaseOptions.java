package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

import java.util.List;

/**
 * Created by greg on 5/21/16.
 */
@CommandLineInterface(application = "run")
public interface RunBaseOptions extends FollowOptions,OptionalProjectOptions {
    @Option(shortName = "l",
            longName = "logevel",
            description = "Run the command using the specified LEVEL. LEVEL can be verbose, info, warning, error.",
            defaultValue = {"info"},
            pattern = "(verbose|info|warning|error)")
    String getLoglevel();

    @Option(shortName = "j",
            longName = "job",
            description = "Job job (group and name). Run a Job specified by Job name and group. eg: 'group/name'.")
    String getJob();

    boolean isJob();

    @Option(shortName = "i", longName = "id", description = "Run the Job with this IDENTIFIER")
    String getId();

    boolean isId();

    @Option(shortName = "F", longName = "filter", description = "A node filter string")
    String getFilter();

    boolean isFilter();

    @Unparsed(name = "-- -ARG VAL -ARG2 VAL", description = "Dispatch specified command string")
    List<String> getCommandString();

}

