package org.rundeck.util.toolbelt;

/**
 * Can format output objects
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
