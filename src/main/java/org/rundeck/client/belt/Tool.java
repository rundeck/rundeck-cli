package org.rundeck.client.belt;

/**
 * Manages commands, runs them
 */
public interface Tool {
    boolean run(String[] args) throws CommandRunFailure;

    boolean runCommand(String name, String[] args) throws CommandRunFailure;
}
