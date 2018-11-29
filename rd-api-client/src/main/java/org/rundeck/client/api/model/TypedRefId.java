package org.rundeck.client.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TypedRefId
        extends RefId
        implements DataOutput
{
    protected String type;

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("type", type);
        return map;
    }
}
