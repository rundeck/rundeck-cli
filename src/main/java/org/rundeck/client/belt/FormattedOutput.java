package org.rundeck.client.belt;

import java.io.IOException;

/**
 * Created by greg on 5/23/16.
 */
public class FormattedOutput implements CommandOutput {
    CommandOutput delegate;
    OutputFormatter formatter;

    public FormattedOutput(
            final CommandOutput output,
            final OutputFormatter formatter
    )
    {
        this.delegate = output;
        this.formatter = formatter;
    }

    @Override
    public void output(final Object output)  {
        delegate.output(formatter.format(output));

    }

    @Override
    public void error(final Object error)  {
        delegate.error(formatter.format(error));
    }

    @Override
    public void warning(final Object error)  {
        delegate.warning(formatter.format(error));
    }

}
