package org.rundeck.client.api.model.pro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.rundeck.client.api.model.DateInfo;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reaction
        implements DataOutput
{
    private String uuid;
    private String name;
    private String description;
    private String project;
    private String serverUuid;
    private String author;
    private DateInfo dateCreated;
    private DateInfo lastUpdated;
    private boolean enabled;
    private Map selector;
    private List<HandlerConfig> handlers;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uuid", getUuid());
        map.put("name", getName());
        map.put("description", getDescription());
        map.put("project", getProject());
        map.put("serverUuid", getServerUuid());
        map.put("author", getAuthor());
        map.put("dateCreated", getDateCreated().asMap());
        map.put("lastUpdated", getLastUpdated().asMap());
        map.put("enabled", isEnabled());
        map.put("selector", getSelector());
        map.put("handlers", getHandlers());
        return map;
    }

    public String toBasicString() {
        return String.format(
                "%s %s%s%s",
                uuid,
                name,
                description != null ? " - " + description : "",
                enabled ? "" : " (disabled)"
        );
    }

    @Data

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HandlerConfig
            implements DataOutput
    {
        private String type;
        private Map<String, Object> config;

        @Override
        public Map<?, ?> asMap() {
            HashMap<String, Object> map = new HashMap<>();
            map.put("type", getType());
            map.put("config", getConfig());
            return map;
        }
    }
}
