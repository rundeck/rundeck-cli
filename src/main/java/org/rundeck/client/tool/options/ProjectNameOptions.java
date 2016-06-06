package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

import java.util.List;

/**
 * Project name
 */
public interface ProjectNameOptions extends BaseOptions {

    @Option(shortName = "p", longName = "project", description = "Project name")
    String getProject();


}
