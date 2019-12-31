package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

public interface ProjectExecutionModeLaterOptions extends BaseOptions {
    
    @Option(shortName = "p", longName = "project", description = "Project Name")
    String getProject();


    @Option(shortName = "t", longName = "type", description = "Type: executions or schedule")
    String getType();

    @Option(shortName = "v", longName = "timeValue", description = "Set the time value where the execution will be enable/disable. For example: 3m, 1h, 5d")
    String getTimeValue();
}
