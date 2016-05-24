package org.rundeck.client.belt;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by greg on 5/23/16.
 */
public class FormattedOutput implements CommandOutput {
    OutputStreamWriter writer;
    OutputStreamWriter errorWriter;
    OutputFormatter formatter;

    public FormattedOutput(
            final OutputStreamWriter writer,
            final OutputStreamWriter errorWriter,
            final OutputFormatter formatter
    )
    {
        this.writer = writer;
        this.errorWriter = errorWriter;
        this.formatter = formatter;
    }

    @Override
    public void output(final Object output) throws IOException {
        writer.write(formatter.format(output));

    }

    @Override
    public void error(final Object error) throws IOException {
        errorWriter.write(formatter.format(error));
    }

    @Override
    public void warning(final Object error) throws IOException {
        errorWriter.write(formatter.format(error));
    }

}
