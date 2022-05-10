package org.rundeck.client.tool.format;

import java.util.regex.Pattern;
import static org.rundeck.client.tool.format.NiceFormatter.NL;

public class PrefixFormatter implements OutputFormatter {
    final String prefix;
    final OutputFormatter base;
    private boolean truncateFinalNewline = true;

    public PrefixFormatter(final String prefix) {
        this.prefix = prefix;
        this.base = new ToStringFormatter();
    }

    public PrefixFormatter(final String prefix, final OutputFormatter base) {
        this.prefix = prefix;
        this.base = base;
    }

    @Override
    public String format(final Object o) {
        return addPrefix(prefix, null != base ? base.format(o) : o.toString());
    }

    private String addPrefix(final String prefix, final String text) {
        StringBuilder sb = new StringBuilder();
        indent(text, sb, true, prefix);
        return sb.toString();
    }

    private void indent(final String text, final StringBuilder sb, final boolean firstLine, final String prefix) {
        if (text.contains(NL)) {
            String[] split = text.split(Pattern.quote(NL), -1);
            int length = split.length;
            if (text.endsWith(NL)) {
                length--;
            }
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    sb.append(NL);
                }
                sb.append(prefix).append(split[i]);
            }
            if (text.endsWith(NL) && !isTruncateFinalNewline()) {
                sb.append(NL);
            }
        } else {
            sb.append(prefix).append(text);
        }
    }

    @Override
    public OutputFormatter withBase(final OutputFormatter base) {
        return new PrefixFormatter(prefix, base);
    }

    public boolean isTruncateFinalNewline() {
        return truncateFinalNewline;
    }

    public void setTruncateFinalNewline(boolean truncateFinalNewline) {
        this.truncateFinalNewline = truncateFinalNewline;
    }
}