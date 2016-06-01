package org.rundeck.util.toolbelt.input.jewelcli;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import org.rundeck.util.toolbelt.CommandInput;
import org.rundeck.util.toolbelt.InputError;

import java.util.List;

/**
 * Parse using JewelCLI
 */
public class JewelInput implements CommandInput {
    @Override
    public <T> T parseArgs(
            final String command, final String[] args, final Class<? extends T> clazz, final String paramName
    ) throws InputError
    {
        try {
            return CliFactory.parseArguments(clazz, args);
        } catch (ArgumentValidationException e) {
            throw new InputError(e.getMessage(), e);
        }
    }

    @Override
    public String getHelp(final String command, final Class<?> type, final String paramName) {
        Cli<?> cli = CliFactory.createCli(type);
        return cli.getHelpMessage();
    }
}
