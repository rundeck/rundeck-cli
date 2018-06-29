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

package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author greg
 * @since 3/1/17
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobFileUploadResult implements DataOutput {
    private Integer total;
    private Map<String, String> options;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("options", options);
        return map;
    }

    /**
     * @return the result file ID for the given option if available, or null
     */
    public String getFileIdForOption(String option) {
        if (getTotal() > 0 && null != getOptions() && null != getOptions().get(option)) {
            return getOptions().get(option);
        }
        return null;
    }
}
