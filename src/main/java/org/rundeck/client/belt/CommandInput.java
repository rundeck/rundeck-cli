package org.rundeck.client.belt;

/**
 * Created by greg on 5/23/16.
 */
public interface CommandInput {
    <T> T parseArgs(String[] args, Class<? extends T> clazz) throws InputError;

    /**
     * Return help string for the option type
     * @param type
     * @return
     */
    String getHelp(Class<?> type);
}
