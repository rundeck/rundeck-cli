package org.rundeck.client.tool.format;

public interface OutputFormatter {
    String format(Object o);

    OutputFormatter withBase(OutputFormatter base);
}
