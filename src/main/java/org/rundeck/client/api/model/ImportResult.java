package org.rundeck.client.api.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
@Root(strict=false)
public class ImportResult {
    @ElementList()
    private List<JobLoadItem> succeeded;
    @ElementList()
    private List<JobLoadItem> failed;
    @ElementList()
    private List<JobLoadItem> skipped;

    public List<JobLoadItem> getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(List<JobLoadItem> succeeded) {
        this.succeeded = succeeded;
    }

    public List<JobLoadItem> getFailed() {
        return failed;
    }

    public void setFailed(List<JobLoadItem> failed) {
        this.failed = failed;
    }

    public List<JobLoadItem> getSkipped() {
        return skipped;
    }

    public void setSkipped(List<JobLoadItem> skipped) {
        this.skipped = skipped;
    }


}
