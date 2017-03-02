package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simplifyops.toolbelt.Formatable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author greg
 * @since 3/1/17
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobFileUploadResult implements Formatable {
    private Integer total;
    private Map<String, String> options;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public List<?> asList() {
        return null;
    }

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("options", options);
        return map;
    }
}
