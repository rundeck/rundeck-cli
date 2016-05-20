package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by greg on 5/20/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Root(strict=false)
public class JobLoadItem extends JobItem {

    @Element(required = false)
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toBasicString() {
        if (null != error) {
            return String.format(
                    "[%s] %s%s\n\t:%s",
                    getId() != null ? getId() : "?",
                    getGroup() != null ? getGroup() + "/" : "",
                    getName(),
                    getError()
            );
        } else {
            return super.toBasicString();
        }
    }
}
