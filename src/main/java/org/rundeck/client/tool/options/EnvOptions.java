package org.rundeck.client.tool.options;

import com.simplifyops.toolbelt.InputError;

/**
 * @author greg
 * @since 1/11/17
 */
public interface EnvOptions {
    public String projectOrEnv(ProjectNameOptions options) throws InputError;
}
