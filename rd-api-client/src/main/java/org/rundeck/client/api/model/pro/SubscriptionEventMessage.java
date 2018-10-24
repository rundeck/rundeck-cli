package org.rundeck.client.api.model.pro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.rundeck.client.api.model.DateInfo;
import org.rundeck.client.util.DataOutput;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionEventMessage
        implements DataOutput
{
    String logLevel;
    String message;
    DateInfo time;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("logLevel", logLevel);
        map.put("message", message);
        map.put("time", time.asMap());
        return map;
    }

    public String toBasicString() {
        try {
            return String.format("[%s:%s] %s", logLevel, time.toRelative(), message);
        } catch (ParseException e) {
            return String.format("[%s:%s] %s", logLevel, time.toString(), message);
        }
    }
}
