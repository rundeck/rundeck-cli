package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Unparsed;

import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
public interface ProjectCreateOptions extends ProjectOptions {

    @Unparsed(name = "-- -configkey=value",defaultValue = {})
    List<String> config();
}
