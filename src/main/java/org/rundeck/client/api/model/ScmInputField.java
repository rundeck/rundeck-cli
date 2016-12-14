package org.rundeck.client.api.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
public class ScmInputField {
    public String defaultValue;
    public String description;
    public String name;
    public Boolean required;
    public Map<String, String> renderingOptions;
    public String scope;
    public String title;
    public String type;
    public List<String> values;

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("title", title);
        map.put("description", description);
        map.put("defaultValue", defaultValue);
        map.put("required", required);
        map.put("scope", scope);
        map.put("renderingOptions", renderingOptions);
        map.put("values", values);
        return map;
    }
}
