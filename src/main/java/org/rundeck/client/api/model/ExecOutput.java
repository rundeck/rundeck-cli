package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecOutput {
    public String id;
    public String message;
    public String error;
    public boolean unmodified;
    public boolean empty;
    public long offset;
    public boolean completed;
    public boolean execCompleted;
    public boolean hasFailedNodes;
    public String execState;
    public long lastModified;
    public long execDuration;
    public float percentLoaded;
    public int totalSize;
    public List<ExecLog> entries;


}
