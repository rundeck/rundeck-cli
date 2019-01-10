package org.rundeck.client.api.model.pro;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionValidationError {
    private String message;
    private List<HandlerValidationError> reports;

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        reports.forEach(rpt -> map.put(rpt.getKey(), rpt.getErrors()));
        return map;
    }

    @Data

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HandlerValidationError extends PluginValidationError{
        private String key;
    }
}
