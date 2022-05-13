package org.rundeck.client.tool.format;

import java.util.List;
import java.util.Map;

public interface Formatable {
    default List<?> asList() {
        return null;
    }

    default Map<?, ?> asMap() {
        return null;
    }
}
