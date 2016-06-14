package org.rundeck.client.api.model.sysinfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by greg on 6/13/16.
 */
public class Link {
    private String href;
    private String contentType;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> toMap() {
        Map<String, String> data = new HashMap<>();
        data.put("href", href);
        data.put("contentType", contentType);
        return data;
    }

    @Override
    public String toString() {
        return "{" + "\n" +
               "href='" + href + '\'' + "\n" +
               ", contentType='" + contentType + '\'' + "\n" +
               '}';
    }
}
