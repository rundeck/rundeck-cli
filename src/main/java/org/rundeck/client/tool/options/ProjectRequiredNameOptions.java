package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * Required project name option
 * @author greg
 * @since 4/10/17
 */
public interface ProjectRequiredNameOptions {
    @Option(shortName = "p", longName = "project", description = "Project name")
    String getProject();
}
