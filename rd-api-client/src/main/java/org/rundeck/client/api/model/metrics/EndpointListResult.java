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

package org.rundeck.client.api.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.rundeck.client.api.model.sysinfo.Link;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class EndpointListResult {

    private Map<String, Link> endpointLinks;

    public int size() {
        return endpointLinks != null ? endpointLinks.size() : 0;
    }

    @JsonProperty("_links")
    public Map<String, Link> getEndpointLinks() {
        return endpointLinks;
    }

    @JsonProperty("_links")
    public EndpointListResult setEndpointLinks(Map<String, Link> endpointLinks) {
        this.endpointLinks = endpointLinks;
        return this;
    }

    @Override
    public String toString() {
        return "EndpointListResult{" +
            "endpointLinks=" + endpointLinks +
            '}';
    }
}
