package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 5/20/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecLog {
    public String time;
    public String level;
    public String log;
    public String user;
    public String command;
    public String node;

}
