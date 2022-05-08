package org.rundeck.client.tool.format;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Optional;
import java.util.function.Function;

/**
 * Format objects as YAML, this will convert any Map/Collection into Yaml, and any Object that implements {@link
 * Formatable} and returns a non-null Map or List. If the object does not correspond to one of those inputs, the base
 * formatter will be used
 */
public class YamlFormatter extends BaseDataOutputFormatter {
    private final Yaml yaml;
    /**
     */
    public YamlFormatter() {
        this(new ToStringFormatter());
    }

    /**
     */
    public YamlFormatter(Representer representer, DumperOptions options) {
        this(new Yaml(representer, options), new ToStringFormatter());
    }

    public YamlFormatter(
            final Function<Object, Optional<Formatable>> dataFormatter,
            final Yaml yaml
    ) {
        super(dataFormatter);
        this.yaml = yaml;
    }


    /**
     * @param base base formatter
     */
    public YamlFormatter(final OutputFormatter base) {
        this(new Yaml(), base);
    }

    public YamlFormatter(
            final OutputFormatter base,
            final Function<Object, Optional<Formatable>> dataFormatter,
            final Yaml yaml
    ) {
        super(base, dataFormatter);
        this.yaml = yaml;
    }

    /**
     * @param base base formatter
     */
    private YamlFormatter(Yaml yaml, final OutputFormatter base) {
        super(base);
        this.yaml = yaml;
    }

    @Override
    protected OutputFormatter withBase(
            final Function<Object, Optional<Formatable>> dataFormatter, final OutputFormatter base
    ) {
        return new YamlFormatter(base, dataFormatter, yaml);
    }

    /**
     * @param base    base formatter
     * @param options yaml options
     */
    public YamlFormatter(final OutputFormatter base, DumperOptions options) {
        super(base);
        this.yaml = new Yaml(options);
    }

    @Override
    protected boolean canFormatObject(final Object value) {
        return true;
    }

    @Override
    protected String formatObject(final Object o) {
        return yaml.dump(o);
    }
}
