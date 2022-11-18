package org.rundeck.client.tool.commands.enterprise.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ResumeResponse implements DataOutput {
    boolean successful;
    String executionId;
    String href;
    String permalink;
    String message;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("executionId", executionId);
        map.put("successful", Boolean.toString(successful));
        map.put("message", message);
        map.put("href", href);
        map.put("permalink", permalink);
        return map;
    }
}
