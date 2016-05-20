package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteJobsResult {
    private boolean allsuccessful;
    private int requestCount;
    private List<DeleteJob> succeeded;
    private List<DeleteJob> failed;

    public boolean isAllsuccessful() {
        return allsuccessful;
    }

    public void setAllsuccessful(boolean allsuccessful) {
        this.allsuccessful = allsuccessful;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public List<DeleteJob> getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(List<DeleteJob> succeeded) {
        this.succeeded = succeeded;
    }

    public List<DeleteJob> getFailed() {
        return failed;
    }

    public void setFailed(List<DeleteJob> failed) {
        this.failed = failed;
    }
}
