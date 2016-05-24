package org.rundeck.client.belt;

/**
 * Created by greg on 5/24/16.
 */
public class SameFormatter implements OutputFormatter {
    @Override
    public String format(final Object o) {
        return o.toString();
    }
}
