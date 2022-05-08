package org.rundeck.client.tool.output;


import org.rundeck.client.tool.CommandOutput;

import java.util.function.BiConsumer;

/**
 * Channels output to another output based on the method
 */
public class ChannelOutput implements CommandOutput {
    CommandOutput fallback;
    boolean infoEnabled = true;
    CommandOutput info;
    boolean outputEnabled = true;
    CommandOutput output;
    boolean warningEnabled = true;
    CommandOutput warning;
    boolean errorEnabled = true;
    CommandOutput error;

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ChannelOutput orig) {
        return new Builder(orig);
    }

    public static class Builder {
        ChannelOutput build;

        public Builder() {
            build = new ChannelOutput();
        }

        public Builder(ChannelOutput output) {
            build = new ChannelOutput(output);
        }


        public Builder fallback(final CommandOutput fallback) {
            build.fallback = fallback;
            return this;
        }

        public Builder info(final CommandOutput info) {
            build.info = info;
            return this;
        }

        public Builder infoEnabled(final boolean enabled) {
            build.infoEnabled = enabled;
            return this;
        }

        public Builder output(final CommandOutput output) {
            build.output = output;
            return this;
        }

        public Builder outputEnabled(final boolean enabled) {
            build.outputEnabled = enabled;
            return this;
        }

        public Builder warning(final CommandOutput warning) {
            build.warning = warning;
            return this;
        }

        public Builder warningEnabled(final boolean enabled) {
            build.warningEnabled = enabled;
            return this;
        }

        public Builder error(final CommandOutput error) {
            build.error = error;
            return this;
        }

        public Builder errorEnabled(final boolean enabled) {
            build.errorEnabled = enabled;
            return this;
        }

        public ChannelOutput build() {
            return new ChannelOutput(build);
        }
    }

    private ChannelOutput(
    )
    {
    }

    private ChannelOutput(ChannelOutput output) {
        this.fallback = output.fallback;
        this.info = output.info;
        this.output = output.output;
        this.warning = output.warning;
        this.error = output.error;
        this.infoEnabled = output.infoEnabled;
        this.outputEnabled = output.outputEnabled;
        this.warningEnabled = output.warningEnabled;
        this.errorEnabled = output.errorEnabled;

    }

    private void select(
            final CommandOutput candidate,
            final CommandOutput fallback,
            final Object output,
            BiConsumer<CommandOutput, Object> out
    )
    {
        if (null != candidate) {
            out.accept(candidate, output);
        } else if (null != fallback) {
            out.accept(fallback, output);
        }
    }

    @Override
    public void info(final Object msg) {
        if (infoEnabled) {
            select(info, fallback, msg, CommandOutput::info);
        }
    }

    @Override
    public void output(final Object msg) {

        if (outputEnabled) {
            select(output, fallback, msg, CommandOutput::output);
        }
    }

    @Override
    public void error(final Object msg) {
        if (errorEnabled) {
            select(error, fallback, msg, CommandOutput::error);
        }
    }

    @Override
    public void warning(final Object msg) {
        if (warningEnabled) {
            select(warning, fallback, msg, CommandOutput::warning);
        }
    }
}