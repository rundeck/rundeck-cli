package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 8/9/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiToken {
    private String id;
    private String user;

    @Override
    public String toString() {
        return "API Token: " + getTruncatedId();
    }
    public String toFullString() {
        return "API Token: " + id;
    }


    public String getTruncatedId() {
        return id != null ? id.substring(0, 5) + "*****" : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
