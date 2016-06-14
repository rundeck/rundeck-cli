package org.rundeck.util.toolbelt;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by greg on 6/13/16.
 */
public class ANSIColorOutput implements CommandOutput, OutputFormatter {
    private static final Object ESC = "\u001B";
    private static final Object RESET = ESC + "[0m";
    private static final Object RED = ESC + "[31m";
    private static final Object YELLOW = ESC + "[33m";

    SystemOutput sink;

    public ANSIColorOutput(final SystemOutput sink) {
        this.sink = sink;
    }

    @Override
    public String format(final Object o) {
        return toColors(o);
    }

    @Override
    public void output(final Object object) {
        sink.output(toColors(object));
    }

    private String toColors(final Object object) {
        if (ColorString.class.isAssignableFrom(object.getClass())) {
            ColorString object1 = (ColorString) object;
            Set<ColorArea> colors = object1.getColors();
            String string = object1.toString();
            int cur = 0;
            int count = 0;
            StringBuilder sb = new StringBuilder();
            for (ColorArea area : colors) {
                if (count > 0) {
                    sb.append(RESET);
                    count--;
                }
                if (area.getStart() > cur) {
                    sb.append(string.substring(cur, area.getStart()));
                }
                cur = area.getStart();
                sb.append(area.getColor().toString());
                if (area.getLength() > 0) {
                    sb.append(string.substring(cur, cur + area.getLength()));
                    cur += area.getLength();
                    sb.append(RESET);
                }else {

                    count++;
                }
            }
            if (cur < string.length() - 1) {
                sb.append(string.substring(cur));
            }

            if (count > 0) {
                sb.append(RESET);
            }
            return sb.toString();
        } else {
            return object.toString();
        }
    }

    @Override
    public void error(final Object error) {
        sink.errorPrint(RED);
        sink.error(error);
        sink.errorPrint(RESET);
    }


    @Override
    public void warning(final Object error) {
        sink.errorPrint(YELLOW);
        sink.warning(error);
        sink.errorPrint(RESET);
    }

    public static enum Color {

        RESET("0"),
        RED("31"),
        ORANGE("38;5;208"),
        GREEN("32"),
        YELLOW("33"),
        BLUE("34"),
        INDIGO("38;5;90"),
        VIOLET("38;5;165"),
        MAGENTA("35"),
        WHITE("37");
        String code;

        Color(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return ESC + "[" + code + "m";
        }
    }

    public static interface ColorArea extends Comparable<ColorArea> {
        default int getStart() {
            return 0;
        }

        default int getLength() {
            return -1;
        }

        Color getColor();

        default int compareTo(ColorArea ca) {
            return getStart() < ca.getStart() ? -1 :
                   getStart() > ca.getStart() ? 1 :
                   0;
        }
    }

    /**
     * A String which defines colorized portions
     */
    public static interface ColorString {
        Set<ColorArea> getColors();
    }

    public static class Colorized implements ColorString {
        Set<ColorArea> colors;
        String value;

        public Colorized(final Set<ColorArea> colors, final String value) {
            this.colors = colors;
            this.value = value;
        }

        @Override
        public Set<ColorArea> getColors() {
            return colors;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static ColorString colorize(Color color, String string) {
        final Set<ColorArea> colors = new TreeSet<>();
        colors.add(() -> color);
        return new Colorized(colors, string);
    }

    public static ColorString colorize(String prefix, final Color color, String wrapped) {
        return colorize(prefix, color, wrapped, "");
    }

    public static ColorString colorize(final Color color, String wrapped, String suffix) {
        return colorize("", color, wrapped, suffix);
    }

    public static ColorString colorize(String prefix, final Color color, String wrapped, String suffix) {
        final Set<ColorArea> colors = new TreeSet<>();
        colors.add(new ColorArea() {
            @Override
            public Color getColor() {
                return color;
            }

            @Override
            public int getStart() {
                return prefix.length();
            }

            @Override
            public int getLength() {
                return wrapped.length();
            }
        });
        return new Colorized(colors, prefix + wrapped + suffix);
    }
}
