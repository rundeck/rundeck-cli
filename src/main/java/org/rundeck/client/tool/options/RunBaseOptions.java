package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;
import org.rundeck.client.api.model.DateInfo;

import java.util.Date;
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

    @Option(shortName = "u", longName = "user", description = "A username to run the job as, (runAs access required).")
    String getUser();

    boolean isUser();

    @Option(shortName = "@",
            longName = "at",
            description = "Run the job at the specified date/time. ISO8601 format (yyyy-MM-dd'T'HH:mm:ss'Z')")
    DateInfo getRunAtDate();

    boolean isRunAtDate();

    @Unparsed(name = "-- -OPT VAL -OPT2 VAL", description = "Job options")
    List<String> getCommandString();

}

