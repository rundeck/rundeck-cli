package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by greg on 5/20/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Execution {
    private String id;
    private String href;
    private String permalink;
    private String status;
    private String project;
    private String user;
    private String serverUUID;

    @JsonProperty("date-started")
    private DateInfo dateStarted;
    @JsonProperty("date-ended")
    private DateInfo dateEnded;
    private JobItem job;
    private String description;
    private String argstring;
    private List<String> successfulNodes;
    private List<String> failedNodes;


    public String toBasicString() {
        String desc = getBasicDescription();
        if (null != desc) {
            return String.format("[%s] %s <%s>", id, desc, permalink);
        }
        return String.format("[%s] <%s>", id, permalink);
    }

    public String toExtendedString() {
        return String.format(
                "%s %s %s %s %s %s %s",
                id,
                status,
                null != dateStarted ? dateStarted.format() : "-",
                null != dateEnded ? dateEnded.format() : "-",
                permalink,
                null != getJob() ? "job" : "adhoc",
                getBasicDescription()
        );
    }

    private String getBasicDescription() {
        if (null != getJob()) {
            return getJob().toBasicString();
        }
        return description;
    }

    public Map getInfoMap()  {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("id", getId());
        map.put("description", getDescription());
        map.put("argstring", getArgstring());
        map.put("permalink", getPermalink());
        map.put("href", getHref());
        map.put("status", getStatus());
        map.put("project", getProject());
        map.put("job", getJob());
        map.put("user", getUser());
        map.put("serverUUID", getServerUUID());
        map.put("dateStarted", null != getDateStarted() ? getDateStarted().format() : null);
        map.put("dateEnded", null != getDateEnded() ? getDateEnded().format() : null);
        map.put("successfulNodes", getSuccessfulNodes());
        map.put("failedNodes", getFailedNodes());
        return map;
    }

    public String toStatusString() {
        return String.format("[%s] %s", id, status);
    }

    @Override
    public String toString() {
        return "org.rundeck.client.api.model.Execution{" +
               "id='" + id + '\'' +
               ", href='" + href + '\'' +
               ", permalink='" + permalink + '\'' +
               ", status='" + status + '\'' +
               ", project='" + project + '\'' +
               ", user='" + user + '\'' +
               ", serverUUID='" + serverUUID + '\'' +
               ", dateStarted=" + dateStarted +
               ", dateEnded=" + dateEnded +
               ", job=" + job +
               ", description='" + description + '\'' +
               ", argstring='" + argstring + '\'' +
               ", successfulNodes=" + successfulNodes +
               ", failedNodes=" + failedNodes +
               '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getServerUUID() {
        return serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
    }

    public DateInfo getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(DateInfo dateStarted) {
        this.dateStarted = dateStarted;
    }

    public DateInfo getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(DateInfo dateEnded) {
        this.dateEnded = dateEnded;
    }

    public JobItem getJob() {
        return job;
    }

    public void setJob(JobItem job) {
        this.job = job;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArgstring() {
        return argstring;
    }

    public void setArgstring(String argstring) {
        this.argstring = argstring;
    }

    public List<String> getSuccessfulNodes() {
        return successfulNodes;
    }

    public void setSuccessfulNodes(List<String> successfulNodes) {
        this.successfulNodes = successfulNodes;
    }

    public List<String> getFailedNodes() {
        return failedNodes;
    }

    public void setFailedNodes(List<String> failedNodes) {
        this.failedNodes = failedNodes;
    }

}
