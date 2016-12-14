package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmImportItem {
    public String itemId;
    public Boolean tracked;
    public ScmJobItem job;

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("tracked", tracked);
        map.put("job", job.toMap());
        return map;
    }
}
