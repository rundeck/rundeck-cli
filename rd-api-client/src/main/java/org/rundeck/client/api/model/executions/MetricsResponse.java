package org.rundeck.client.api.model.executions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MetricsResponse
        implements DataOutput
{
    private Long total;
    private Status status;
    private Map<String, String> duration;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("status", status);
        data.put("duration", duration);
        return data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Status
            implements DataOutput
    {
        private Long succeeded;
        private Long failed;
        @JsonProperty("failed-with-retry")
        private Long failedWithRetry;
        private Long aborted;
        private Long running;
        private Long other;
        private Long timedout;
        private Long scheduled;

        @Override
        public Map<?, ?> asMap() {
            HashMap<String, Object> data = new HashMap<>();
            if (null != succeeded) {
                data.put("succeeded", succeeded);
            }
            if (null != failed) {
                data.put("failed", failed);
            }
            if (null != failedWithRetry) {
                data.put("failed-with-retry", failedWithRetry);
            }
            if (null != aborted) {
                data.put("aborted", aborted);
            }
            if (null != running) {
                data.put("running", running);
            }
            if (null != other) {
                data.put("other", other);
            }
            if (null != timedout) {
                data.put("timedout", timedout);
            }
            if (null != scheduled) {
                data.put("scheduled", scheduled);
            }
            return data;
        }
    }
}
