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
public class ResumeStatus implements DataOutput {
    String executionId;
    boolean resumeable;
    String message;


    @Override
    public Map<?, ?> asMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("executionId", executionId);
        map.put("resumable", Boolean.toString(resumeable));
        map.put("message", message);
        return map;
    }
}
