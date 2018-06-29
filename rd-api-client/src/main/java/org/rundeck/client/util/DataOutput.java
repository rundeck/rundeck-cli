package org.rundeck.client.util;

import java.util.List;
import java.util.Map;

public interface DataOutput {
    default Map<?, ?> asMap() {
        return null;
    }

    default List<?> asList() {
        return null;
    }
}
