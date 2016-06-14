package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.rundeck.client.api.model.sysinfo.SystemStats;

/**
 * Created by greg on 6/13/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemInfo {

    public SystemStats system;

    @Override
    public String toString() {
        return "{" +"\n"+
               "system=" + system +"\n"+
               '}';
    }
}
