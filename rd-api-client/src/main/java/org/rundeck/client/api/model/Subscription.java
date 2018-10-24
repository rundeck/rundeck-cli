package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subscription
        implements DataOutput
{
    private String uuid;
    private String project;
    private String serverUuid;
    private String author;
    private DateInfo lastUpdated;
    private DateInfo dateCreated;
    private String type;
    private boolean enabled;
    private Map config;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("uuid", getUuid());
        data.put("project", getProject());
        data.put("serverUuid", getServerUuid());
        data.put("author", getAuthor());
        data.put("dateCreated", getDateCreated().asMap());
        data.put("lastUpdated", getLastUpdated().asMap());
        data.put("type", getType());
        data.put("enabled", isEnabled());
        data.put("config", getConfig());
        return data;
    }

    public String toBasicString() {
        return String.format("%s %s%s", uuid, type,
                             enabled ? "" : " (disabled)"
        );
    }
}
