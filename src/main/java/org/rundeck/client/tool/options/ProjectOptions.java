package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

import java.util.List;

/**
 * Created by greg on 5/19/16.
 */
public interface ProjectOptions extends BaseOptions {

    @Option(shortName = "p", longName = "project", description = "Project name")
    String getProject();


}
