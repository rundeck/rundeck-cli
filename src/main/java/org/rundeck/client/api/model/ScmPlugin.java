package org.rundeck.client.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
public class ScmPlugin {
    public String type;
    public String title;
    public String description;
    public Boolean configured;
    public Boolean enabled;

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("title", title);
        map.put("description", description);
        map.put("configured", configured);
        map.put("enabled", enabled);
        return map;
    }
}
