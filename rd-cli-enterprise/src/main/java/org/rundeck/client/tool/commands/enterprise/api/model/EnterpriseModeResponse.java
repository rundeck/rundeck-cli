package org.rundeck.client.tool.commands.enterprise.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.rundeck.client.api.model.SystemMode;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EnterpriseModeResponse extends SystemMode implements DataOutput {
    String status;
    String uuid;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("status", status);
        map.put("mode", getExecutionMode().toString());
        map.put("uuid", uuid);
        return map;
    }
}
