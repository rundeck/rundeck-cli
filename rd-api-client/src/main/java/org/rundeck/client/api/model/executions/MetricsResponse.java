package org.rundeck.client.api.model.executions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.rundeck.client.util.DataOutput;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

@Root(strict = false, name = "result")
@Data
public class MetricsResponse
        implements DataOutput
{
    private Long total;
    private Long failed;
    @JsonProperty(value = "failed-with-retry")
    @Element(name = "failed-with-retry")
    private Long failedWithRetry;
    private Long succeeded;
    @JsonProperty(value = "duration-avg")
    @Element(name = "duration-avg")
    private String durationAvg;
    @JsonProperty(value = "duration-min")
    @Element(name = "duration-min")
    private String durationMin;
    @JsonProperty(value = "duration-max")
    @Element(name = "duration-max")
    private String durationMax;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("failed-with-retry", failedWithRetry);
        data.put("succeeded", succeeded);
        data.put("failed", failed);
        data.put("duration-avg", durationAvg);
        data.put("duration-min", durationMin);
        data.put("duration-max", durationMax);
        return data;
    }
}
