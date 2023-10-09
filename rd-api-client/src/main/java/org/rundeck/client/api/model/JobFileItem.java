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
import lombok.Data;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;

/**
 * @author greg
 * @since 3/1/17
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobFileItem implements DataOutput {
    private String id;
    private String user;
    private String optionName;
    private String fileState;
    private String sha;
    private String jobId;
    private String dateCreated;
    private String serverNodeUUID;
    private String fileName;
    private Long size;
    private String expirationDate;
    private Long execId;

    public DateInfo dateInfoDateCreated() {
        return new DateInfo(dateCreated);
    }

    public DateInfo dateInfoExpirationDate() {
        return new DateInfo(expirationDate);
    }

    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("user", user);
        map.put("optionName", optionName);
        map.put("fileState", fileState);
        map.put("sha", sha);
        map.put("jobId", jobId);
        map.put("dateCreated", dateCreated);
        map.put("serverNodeUUID", serverNodeUUID);
        map.put("fileName", fileName);
        map.put("size", size);
        map.put("expirationDate", expirationDate);
        map.put("execId", execId);
        return map;
    }
}
