package org.rundeck.client.api.model;

import org.simpleframework.xml.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
public class ScmJobItem {
    public String jobId;
    public String jobName;
    public String groupPath;

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("jobId", jobId);
        map.put("jobName", jobName);
        map.put("groupPath", groupPath);
        return map;
    }
}
