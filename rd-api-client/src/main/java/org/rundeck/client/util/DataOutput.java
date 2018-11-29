package org.rundeck.client.util;

import java.util.*;

public interface DataOutput {
    default Map<?, ?> asMap() {
        return null;
    }

    default List<?> asList() {
        return null;
    }

    static Object collectOutput(DataOutput data) {
        if (data.asMap() != null) {
            return collectMap(data);
        } else if (data.asList() != null) {
            return collectList(data);
        } else {
            return null;
        }
    }

    static List<?> collectList(Collection<?> data) {
        ArrayList<Object> outList = new ArrayList<>();

        if (data != null) {
            for (Object o : data) {
                if (o instanceof DataOutput) {
                    outList.add(collectOutput((DataOutput) o));
                }else{
                    outList.add(o);
                }
            }
        }
        return outList;
    }

    static List<?> collectList(DataOutput data) {
        return collectList(data.asList());
    }

    static Map<?, ?> collectMap(DataOutput data) {
        return collectMap(data.asMap());
    }

    static Map<?, ?> collectMap(final Map<?, ?> dataMap) {
        HashMap<Object, Object> map = new HashMap<>();
        if (dataMap != null) {
            for (Map.Entry<?, ?> o : dataMap.entrySet()) {
                Object value = o.getValue();
                if (value instanceof DataOutput) {
                    DataOutput valueOut = (DataOutput) value;
                    map.put(o.getKey(), collectOutput(valueOut));
                }else{
                    map.put(o.getKey(), value);
                }
            }
        }
        return map;
    }

}
