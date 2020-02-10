package org.rundeck.client.tool.commands;

import org.rundeck.client.tool.InputError;

/**
 * Supplier with throwable
 *
 * @param <T> type
 */
interface GetInput<T> {
    /**
     * @return supplied input
     * @throws InputError if input error
     */
    T get() throws InputError;
}
