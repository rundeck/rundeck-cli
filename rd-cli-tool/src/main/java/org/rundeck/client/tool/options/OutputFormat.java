package org.rundeck.client.tool.options;

public interface OutputFormat {
    String getOutputFormat();

    default boolean isOutputFormat() {
        return getOutputFormat() != null;
    }

    boolean isVerbose();
}
