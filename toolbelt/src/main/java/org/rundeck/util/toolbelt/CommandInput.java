package org.rundeck.util.toolbelt;

/**
 * Parse CLI arguments into input options
 */
public interface CommandInput {
    /**
     * Parse arguments into the given class
     *
     * @param args  arguments
     * @param clazz options class
     * @param <T>   type
     *
     * @return parsed object
     *
     * @throws InputError if  parsing error occurs
     */
    <T> T parseArgs(String[] args, Class<? extends T> clazz) throws InputError;

    /**
     * Return help string for the option type
     *
     * @param type type
     *
     * @return help string
     */
    String getHelp(Class<?> type);
}
