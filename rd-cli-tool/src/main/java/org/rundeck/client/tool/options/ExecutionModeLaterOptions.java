package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

public interface ExecutionModeLaterOptions extends BaseOptions {

    static final String TIME_PATTERN = "^[-_a-zA-Z0-9+][-\\._a-zA-Z0-9+]*$";

    @Option(shortName = "t", longName = "timeValue", description = "Set the time value where the execution will be enable/disable. For example: 3m, 1h, 5d")
    String getTimeValue();
}
