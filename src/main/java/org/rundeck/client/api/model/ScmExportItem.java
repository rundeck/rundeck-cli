package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmExportItem {
    public String itemId;
    public String originalId;
    public ScmJobItem job;
    public Boolean renamed;
    public Boolean deleted;

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        if (null != originalId) {
            map.put("originalId", originalId);
        }
        if (null != job) {
            map.put("job", job.toMap());
        }
        map.put("renamed", renamed);
        map.put("deleted", deleted);
        return map;
    }
}
