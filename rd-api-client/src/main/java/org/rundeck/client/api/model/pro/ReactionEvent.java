package org.rundeck.client.api.model.pro;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.rundeck.client.api.model.DateInfo;
import org.rundeck.client.api.model.RefId;
import org.rundeck.client.api.model.TypedRefId;
import org.rundeck.client.util.DataOutput;
import org.rundeck.client.util.RdClientConfig;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReactionEvent
        implements DataOutput
{
    protected String uuid;
    protected String project;
    protected String status;
    protected String timeZone;
    protected DateInfo dateCreated;
    protected DateInfo lastUpdated;

    protected EventInfo event;
    protected RefId subscription;
    protected RefId reaction;
    protected SourceInfo source;
    protected ResultInfo result;

    public String toExtendedString(final RdClientConfig config) {
        try {
            return String.format(
                    "%s %s %s [%s]",
                    null != dateCreated ? dateCreated.toRelative() : "-",
                    status,
                    result.getMessage(),
                    uuid
            );
        } catch (ParseException e) {
            return String.format(
                    "%s %s %s [%s]",
                    null != dateCreated ? dateCreated.format(config.getDateFormat()) : "-",
                    status,
                    result.getMessage(),
                    uuid
            );
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventInfo
            extends TypedRefId
            implements DataOutput
    {
        protected Map data;

        @Override
        public Map<String, Object> asMap() {
            HashMap<String, Object> map = new HashMap<>(super.asMap());
            map.put("data", data);
            return map;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceInfo
            extends TypedRefId
            implements DataOutput
    {
        protected Map meta;

        @Override
        public Map<String, Object> asMap() {
            HashMap<String, Object> map = new HashMap<>(super.asMap());
            map.put("meta", meta);
            return map;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultInfo
            implements DataOutput
    {
        protected String message;
        protected Map data;
        protected List<TypedRefId> links;

        @Override
        public Map<String, Object> asMap() {
            HashMap<String, Object> map = new HashMap<>();
            if (null != message) {
                map.put("message", message);
            }
            if (null != data) {
                map.put("data", data);
            }
            if (null != links) {
                map.put("links", DataOutput.collectList(links));
            }
            return map;
        }
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uuid", getUuid());
        map.put("project", getProject());
        map.put("status", getStatus());
        map.put("timeZone", getTimeZone());
        map.put("dateCreated", getDateCreated().asMap());
        map.put("lastUpdated", getLastUpdated().asMap());
        map.put("event", DataOutput.collectOutput(getEvent()));
        if (null != getSubscription()) {
            map.put("subscription", DataOutput.collectOutput(getSubscription()));
        }
        if (null != getReaction()) {
            map.put("reaction", DataOutput.collectOutput(getReaction()));
        }
        map.put("source", DataOutput.collectOutput(getSource()));
        map.put("result", DataOutput.collectOutput(getResult()));
        return map;
    }
}
