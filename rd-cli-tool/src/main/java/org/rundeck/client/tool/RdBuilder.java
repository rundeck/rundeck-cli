package org.rundeck.client.tool;

import org.rundeck.client.tool.format.FormattedOutput;
import org.rundeck.client.tool.format.NiceFormatter;
import org.rundeck.client.tool.format.OutputFormatter;
import org.rundeck.client.tool.output.ChannelOutput;
import org.rundeck.client.tool.output.SystemOutput;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class RdBuilder {
    private final Map<Class<? extends Throwable>, ErrorHandler> errorHandlers = new HashMap<>();

    private final ChannelOutput.Builder channels;
    private CommandOutput commandOutput;
    private OutputFormatter formatter;

    private final ANSIColorOutputBuilder ansiBuilder = new ANSIColorOutputBuilder().sink(new SystemOutput());

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
        return ansiBuilder.build();
    }


    public void printStackTrace(boolean b) {

    }

    public OutputFormatter defaultBaseFormatter() {
        return new NiceFormatter(ansiBuilder.build());
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
        OutputFormatter baseFormatter = defaultBaseFormatter();
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

    static final Map<String, String> DEFAULT_COLORS = new HashMap<>();

    static {
        DEFAULT_COLORS.put("info", "green");
        DEFAULT_COLORS.put("warning", "yellow");
        DEFAULT_COLORS.put("error", "red");
    }

    private static class ANSIColorOutputBuilder {
        private SystemOutput sink;
        private final Map<String, String> config = new HashMap<>(DEFAULT_COLORS);

        public ANSIColorOutputBuilder info(String color) {
            config.put("info", color);
            return this;
        }

        public ANSIColorOutputBuilder warning(String color) {
            config.put("warning", color);
            return this;
        }

        public ANSIColorOutputBuilder error(String color) {
            config.put("error", color);
            return this;
        }

        public ANSIColorOutputBuilder output(String color) {
            config.put("output", color);
            return this;
        }

        public ANSIColorOutputBuilder sink(SystemOutput systemOutput) {
            this.sink = systemOutput;
            return this;
        }


        public ANSIColorOutput build() {
            return new ANSIColorOutput(sink, config);
        }
    }

    private static class ANSIColorOutput implements CommandOutput, OutputFormatter {
        private final SystemOutput sink;
        private final Map<String, String> config;

        public ANSIColorOutput(SystemOutput sink, Map<String, String> config) {
            this.sink = sink;
            this.config = config;
        }

        public static String toColors(final Object object) {
            return toColors(object, null);
        }

        @Override
        public String format(Object o) {
            return toColors(o);
        }

        @Override
        public OutputFormatter withBase(OutputFormatter base) {
            return null;
        }

        public static String toColors(final Object object, OutputFormatter base) {
            if (null == object) {
                return null;
            }
//            if (ColorString.class.isAssignableFrom(object.getClass())) {
//                ColorString object1 = (ColorString) object;
//                Set<ColorArea> colors = new TreeSet<>(object1.getColors());
//                String string = null != base ? base.format(object1) : object1.toString();
//                int cur = 0;
//                int count = 0;
//                StringBuilder sb = new StringBuilder();
//                for (ColorArea area : colors) {
//                    if (count > 0) {
//                        sb.append(Color.RESET.toString());
//                        count--;
//                    }
//                    if (area.getStart() >= cur) {
//                        sb.append(string.substring(cur, area.getStart()));
//                    }
//                    cur = area.getStart();
//                    sb.append(area.getColor().toString());
//                    if (area.getLength() > 0) {
//                        sb.append(string.substring(cur, cur + area.getLength()));
//                        cur += area.getLength();
//                        sb.append(Color.RESET.toString());
//                    } else if (area.getColor().compareTo(Color.NONDISPLAYED) > 0) {
//                        count++;
//                    }
//                }
//                if (cur < string.length()) {
//                    sb.append(string.substring(cur));
//                }
//
//                if (count > 0) {
//                    sb.append(Color.RESET.toString());
//                }
//                return sb.toString();
//            } else {
            return null != base ? base.format(object) : object.toString();
//            }
        }

        @Override
        public void info(final Object output) {
            if (output instanceof String && null != config.get("info")) {
                sink.outPrint(toAnsi(output, config.get("info")) + "\n");
            } else {
                sink.info(toColors(output));
            }

        }

        private String toAnsi(Object output, String color) {
            if (color != null) {
                return CommandLine.Help.Ansi.AUTO.string("@|" + color + " " + output + "|@");
            } else {
                return output.toString();
            }
        }

        @Override
        public void output(final Object object) {
            if (object instanceof String && null != config.get("output")) {
                sink.outPrint(toAnsi(object, config.get("output")) + "\n");
            } else {
                sink.output(toColors(object));
            }
        }

        @Override
        public void error(final Object error) {
            if (null != config.get("error")) {
                sink.errorPrint(toAnsi(error, config.get("error")) + "\n");
            } else {
                sink.error(error);
            }
        }


        @Override
        public void warning(final Object error) {
            if (null != config.get("warning")) {
                sink.errorPrint(toAnsi(error, config.get("warning")) + "\n");
            } else {
                sink.error(error);
            }
        }
    }
}
