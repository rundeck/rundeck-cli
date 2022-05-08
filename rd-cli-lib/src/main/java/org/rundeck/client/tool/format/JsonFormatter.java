package org.rundeck.client.tool.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.function.Function;

public class JsonFormatter extends BaseDataOutputFormatter {
    ObjectMapper mapper;

    public JsonFormatter() {
        super();
        this.mapper = new ObjectMapper();
    }

    public JsonFormatter(final Function<Object, Optional<Formatable>> dataFormatter) {
        super(dataFormatter);
        this.mapper = new ObjectMapper();
    }

    public JsonFormatter(final OutputFormatter base) {
        super(base);
        this.mapper = new ObjectMapper();
    }

    public JsonFormatter(
            final OutputFormatter base,
            final Function<Object, Optional<Formatable>> dataFormatter
    ) {
        super(base, dataFormatter);
        this.mapper = new ObjectMapper();
    }

    public JsonFormatter(final ObjectMapper mapper, final OutputFormatter base) {
        super(base);
        this.mapper = mapper;
    }

    public JsonFormatter(
            final OutputFormatter base,
            final Function<Object, Optional<Formatable>> dataFormatter,
            final ObjectMapper mapper
    ) {
        super(base, dataFormatter);
        this.mapper = mapper;
    }

    @Override
    protected String formatObject(final Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean canFormatObject(final Object value) {
        return true;
    }

    @Override
    protected OutputFormatter withBase(
            final Function<Object, Optional<Formatable>> dataFormatter, final OutputFormatter base
    ) {

        return new JsonFormatter(base, dataFormatter, mapper);
    }
}