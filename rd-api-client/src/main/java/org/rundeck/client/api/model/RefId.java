package org.rundeck.client.api.model;

import lombok.Data;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;

@Data
public class RefId
        implements DataOutput
{
    protected String id;

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", getId());
        return map;
    }
}
