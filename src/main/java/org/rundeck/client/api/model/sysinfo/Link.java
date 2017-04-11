/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.api.model.sysinfo;

import java.util.HashMap;
import java.util.Map;

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
