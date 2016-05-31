package org.rundeck.util.toolbelt.input.jewelcli;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import org.rundeck.util.toolbelt.CommandInput;
import org.rundeck.util.toolbelt.InputError;

/**
 * Parse using JewelCLI
 */
public class JewelInput implements CommandInput {
    @Override
    public <T> T parseArgs(final String[] args, final Class<? extends T> clazz) throws InputError {
        try {
            return CliFactory.parseArguments(clazz, args);
        } catch (ArgumentValidationException e) {
            throw new InputError(e.getMessage(), e);
        }
    }

    @Override
    public String getHelp(final Class<?> type) {
        Cli<?> cli = CliFactory.createCli(type);
        return cli.getHelpMessage();
    }
}
