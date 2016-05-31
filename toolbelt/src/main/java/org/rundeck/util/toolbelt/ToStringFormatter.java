package org.rundeck.util.toolbelt;

/**
 * Format object using tostring
 */
public class ToStringFormatter implements OutputFormatter {
    @Override
    public String format(final Object o) {
        return o.toString();
    }
}
