package org.rundeck.client.tool.format;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class BaseDataOutputFormatter implements OutputFormatter {
    OutputFormatter base;
    Function<Object, Optional<Formatable>> dataFormatter;

    public BaseDataOutputFormatter() {
    }

    public BaseDataOutputFormatter(final Function<Object, Optional<Formatable>> dataFormatter) {
        this.dataFormatter = dataFormatter;
    }

    public BaseDataOutputFormatter(final OutputFormatter base) {
        this.base = base;
    }

    public BaseDataOutputFormatter(
            final OutputFormatter base,
            final Function<Object, Optional<Formatable>> dataFormatter
    ) {
        this.base = base;
        this.dataFormatter = dataFormatter;
    }

    @Override
    public String format(final Object o) {
        final Formatable value;
        if (o instanceof Formatable) {
            value = (Formatable) o;
        } else {
            value = null != dataFormatter ? dataFormatter.apply(o).orElse(null) : null;
        }
        if (value != null) {
            List<?> objects = value.asList();
            if (null != objects) {
                return formatList(objects);
            }
            Map<?, ?> map = value.asMap();
            if (null != map) {
                return formatMap(map);
            }
        } else if (canFormatObject(o)) {
            return formatObject(o);
        }
        return null != base ? base.format(o) : o.toString();
    }

    protected String formatMap(Map value) {
        return formatObject(value);
    }

    protected String formatList(List value) {
        return formatObject(value);
    }

    @Override
    public OutputFormatter withBase(final OutputFormatter base) {
        return withBase(dataFormatter, base);
    }

    protected abstract OutputFormatter withBase(
            final Function<Object, Optional<Formatable>> dataFormatter,
            final OutputFormatter base
    );

    protected abstract boolean canFormatObject(Object value);

    protected abstract String formatObject(Object value);

}