package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author greg
 * @since 3/2/17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobFileItemList {
    private Paging paging;
    private List<JobFileItem> files;

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public List<JobFileItem> getFiles() {
        return files;
    }

    public void setFiles(List<JobFileItem> files) {
        this.files = files;
    }
}
