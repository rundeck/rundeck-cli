package org.rundeck.client.tool;

import org.rundeck.client.tool.format.FormattedOutput;
import org.rundeck.client.tool.format.NiceFormatter;
import org.rundeck.client.tool.format.OutputFormatter;
import org.rundeck.client.tool.format.ToStringFormatter;
import org.rundeck.client.tool.options.OutputFormat;
import org.rundeck.client.tool.output.ChannelOutput;
import org.rundeck.client.tool.output.SystemOutput;

import java.util.HashMap;
import java.util.Map;

public class RdBuilder {
    private Map<Class<? extends Throwable>, ErrorHandler> errorHandlers = new HashMap<>();

    private ChannelOutput.Builder channels;
    private CommandOutput commandOutput;
    private OutputFormatter baseFormatter;
    private OutputFormatter formatter;

    public RdBuilder() {
        channels = ChannelOutput.builder();
    }

    /**
     * Handle a throwable type
     */
    public interface ErrorHandler {
        /**
         * Handle the throwable
         *
         * @param throwable throwable
         * @return true if the throwable was consumed, false if it should be rethrown
         */
        boolean handleError(Throwable throwable);
    }

    public void formatter(OutputFormatter yamlFormatter) {

    }

    public ChannelOutput.Builder channels() {
        return channels;
    }

    public CommandOutput defaultOutput() {
        return /*ansiColor ? ansiBuilder.build() : */new SystemOutput();
    }


    public void printStackTrace(boolean b) {

    }

    public OutputFormatter defaultBaseFormatter() {
        return new NiceFormatter(/*ansiColor ? ansiBuilder.build() : */new ToStringFormatter());
    }

    private CommandOutput builtOutput;

    public RdBuilder finalOutput(CommandOutput output) {
        this.builtOutput = output;
        return this;
    }

    public CommandOutput finalOutput() {
        if (null == commandOutput) {
            commandOutput = defaultOutput();
        }
        baseFormatter = defaultBaseFormatter();
        channels.fallback(commandOutput);
        ChannelOutput channel = channels.build();
        if (null == builtOutput) {
            builtOutput = new FormattedOutput(
                    channel,
                    null != formatter ? formatter.withBase(baseFormatter) : baseFormatter
            );
        }
        return builtOutput;
    }

    public <T extends Throwable> RdBuilder handles(Class<T> clazz, ErrorHandler handler) {
        errorHandlers.put(clazz, handler);
        return this;
    }

}
