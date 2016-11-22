package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

/**
 * @author greg
 * @since 11/22/16
 */
public interface QuietOption {
    @Option(shortName = "q", longName = "quiet", description = "Reduce output.")
    boolean isQuiet();
}
