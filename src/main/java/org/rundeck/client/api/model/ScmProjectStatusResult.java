package org.rundeck.client.api.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
public class ScmProjectStatusResult {
    public List<String> actions;
    public String integration;
    public String message;
    public String project;
    public ScmSynchState synchState;

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("synchState", synchState.toString());
        map.put("actions", actions);

        return map;
    }
}
