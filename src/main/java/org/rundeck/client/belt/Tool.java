package org.rundeck.client.belt;

/**
 * a CLI tool
 */
public interface Tool {
    /**
     * Run main arguments
     *
     * @param args       arguments
     * @param exitSystem true to perform System.exit(2) on failure
     *
     * @return true/false if the result succeeded
     *
     * @throws CommandRunFailure
     */
    boolean runMain(String[] args, final boolean exitSystem) throws CommandRunFailure;

    /**
     * run a command by name
     *
     * @param name        command name
     * @param args        command arguments
     * @param inputParser parser for input arguments
     * @param output      output sink for the command
     *
     * @return true if successful
     *
     * @throws CommandRunFailure
     */
    boolean runCommand(String name, String[] args, CommandInput inputParser, CommandOutput output)
            throws CommandRunFailure;
}
