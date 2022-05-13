package org.rundeck.client.tool.format;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class NiceFormatter implements OutputFormatter {
    OutputFormatter base;
    private String collectionIndicator = "* ";
    private String keyValueSeparator = ": ";
    private String indentation = "  ";
    static final String NL = System.getProperty("line.separator");

    public NiceFormatter(final OutputFormatter base) {
        this.base = base;
    }

    @Override
    public OutputFormatter withBase(final OutputFormatter base) {
        this.base = base;
        return this;
    }

    @Override
    public String format(final Object o) {
        if (o instanceof Map) {
            return formatMap((Map) o, 0);
        } else if (o instanceof Collection) {
            return formatCollection((Collection) o, 0);
        }
        return base.format(o);
    }

    private String formatMap(final Map o, final int level) {
        StringBuilder sb = new StringBuilder();
        for (Object key : o.keySet()) {
            indent(level, format(key), sb, true);
            Object o1 = o.get(key);

            String format = format(o1);

            sb.append(keyValueSeparator);
            if(null!=format) {
                if (format.contains(NL)) {
                    sb.append(NL);
                    indent(level + 1, format, sb, true);
                } else {
                    sb.append(format);
                }
            }
            sb.append(NL);
        }
        return sb.toString();
    }

    private void indent(final int level, final String msg, final StringBuilder sb, final boolean firstLine) {
        if (level < 1) {
            sb.append(msg);
            return;
        }
        StringBuilder idsb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            idsb.append(indentation);
        }
        String id = idsb.toString();
        if (msg.contains(NL)) {
            String[] split = msg.split(Pattern.quote(NL));
            if (firstLine) {
                sb.append(id);
            }
            if (split.length > 0) {
                sb.append(split[0]).append(NL);
            }
            for (int i = 1; i < split.length; i++) {
                String s = split[i];
                sb.append(id).append(s).append(NL);
            }
        } else {
            sb.append(id).append(msg);
        }
    }

    private String formatCollection(final Collection o, final int level) {
        StringBuilder sb = new StringBuilder();
        for (Object o1 : o) {
            indent(level, collectionIndicator, sb, true);
            String format = format(o1);
            if (format.contains(NL)) {
                indent(level + 1, format, sb, false);
            } else {
                sb.append(format);
            }
            sb.append(NL);
        }
        return sb.toString();
    }

    public String getCollectionIndicator() {
        return collectionIndicator;
    }

    public void setCollectionIndicator(String collectionIndicator) {
        this.collectionIndicator = collectionIndicator;
    }

    public String getKeyValueSeparator() {
        return keyValueSeparator;
    }

    public void setKeyValueSeparator(String keyValueSeparator) {
        this.keyValueSeparator = keyValueSeparator;
    }

    public String getIndentation() {
        return indentation;
    }

    public void setIndentation(String indentation) {
        this.indentation = indentation;
    }

}