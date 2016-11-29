package org.rundeck.client.tool.options;

import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.util.Env;

/**
 * @author greg
 * @since 11/28/16
 */
public class OptionUtil {
    public static String projectOrEnv(ProjectNameOptions options) throws InputError {
        if (null != options.getProject()) {
            return options.getProject();
        }
        return Env.require("RD_PROJECT", "or specify as `-p/--project value` : Project name.");
    }

}
