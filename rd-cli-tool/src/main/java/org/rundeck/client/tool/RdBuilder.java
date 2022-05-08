package org.rundeck.client.tool;

import org.rundeck.client.tool.format.OutputFormatter;
import org.rundeck.client.tool.output.ChannelOutput;

import java.util.HashMap;
import java.util.Map;

public class RdBuilder {
    private Map<Class<? extends Throwable>, ErrorHandler> errorHandlers = new HashMap<>();

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
        return null;
    }

    public CommandOutput defaultOutput() {
        return null;
    }

    public OutputFormatter defaultBaseFormatter() {
        return null;
    }

    public CommandOutput finalOutput() {
        return null;
    }

    public void printStackTrace(boolean b) {

    }

    public <T extends Throwable> RdBuilder handles(Class<T> clazz, ErrorHandler handler) {
        errorHandlers.put(clazz, handler);
        return this;
    }

}
