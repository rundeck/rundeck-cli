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
 * @since 12/13/16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmImportItem implements DataOutput {
    public String itemId;
    public Boolean tracked;
    public Boolean deleted;
    public ScmJobItem job;
    public String status;

    public Map asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("tracked", tracked);
        map.put("deleted", deleted != null ? deleted : false);
        map.put("status", status);
        if(null!=job) {
            map.put("job", job.toMap());
        }
        return map;
    }
}
