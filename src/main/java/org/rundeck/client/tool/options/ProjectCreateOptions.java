package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Unparsed;

import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
public interface ProjectCreateOptions extends ProjectNameOptions {

    @Unparsed(name = "-- -configkey=value",
              defaultValue = {},
              description = "A set of config properties for the project, in the form --key=value")
    List<String> config();
}
