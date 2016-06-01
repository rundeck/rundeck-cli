package org.rundeck.util.toolbelt;

/**
 * Parse CLI arguments into input options
 */
public interface CommandInput {
    /**
     * Parse one or more arguments for a command. This method will be
     * called when a command is invoked, for each input parameter of the
     * command's method.  The input parser can either parse all
     * arguments into a single object (e.g. Jewel CLI), or parse
     * each parameter individually.
     *
     * @param command   the command
     * @param args      arguments all of the arguments to the command
     * @param clazz     options class the parameter type
     * @param paramName name of parameter
     * @param <T>       type
     *
     * @return parsed object
     *
     * @throws InputError if  parsing error occurs
     */
    <T> T parseArgs(String command, String[] args, Class<? extends T> clazz, String paramName) throws InputError;

    /**
     * Return help string for the option type
     *
     * @param type type
     *
     * @return help string
     */
    String getHelp(String command, Class<?> type, String paramName);
}
