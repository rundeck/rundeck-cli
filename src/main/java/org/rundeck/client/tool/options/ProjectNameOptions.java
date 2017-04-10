package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

import java.util.List;

/**
 * Optional Project name options
 */
public interface ProjectNameOptions extends BaseOptions, ProjectRequiredNameOptions {

    boolean isProject();
}
