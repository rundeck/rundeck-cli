package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * @author greg
 * @since 4/4/17
 */
public interface JobIdentOptions extends OptionalProjectOptions {

    @Option(shortName = "j",
            longName = "job",
            description = "Job job (group and name). Run a Job specified by Job name and group. eg: 'group/name'.")
    String getJob();

    boolean isJob();

    @Option(shortName = "i", longName = "id", description = "Run the Job with this IDENTIFIER")
    String getId();

    boolean isId();

}
