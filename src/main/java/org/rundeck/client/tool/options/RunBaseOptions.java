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
public interface RunBaseOptions extends FollowOptions,OptionalProjectOptions, NodeFilterOptions {
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


    @Option(shortName = "u", longName = "user", description = "A username to run the job as, (runAs access required).")
    String getUser();

    boolean isUser();

    @Option(shortName = "@",
            longName = "at",
            description = "Run the job at the specified date/time. ISO8601 format (yyyy-MM-dd'T'HH:mm:ss'Z')")
    DateInfo getRunAtDate();

    boolean isRunAtDate();

    @Option(shortName = "d",
            longName = "delay",
            description = "Run the job at a certain time from now. Format: ##[smhdwMY] where ## " +
                          "is an integer and the units are seconds, minutes, hours, days, weeks, Months, Years. Can combine " +
                          "units, e.g. \"2h30m\", \"20m30s\"",
            pattern = "(\\d+[smhdwMY]\\s*)+")
    String getRunDelay();

    boolean isRunDelay();

    @Unparsed(name = "-- -OPT VAL -OPT2 VAL -OPTFILE @filepath", description = "Job options")
    List<String> getCommandString();

}

