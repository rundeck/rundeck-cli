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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author greg
 * @since 3/1/17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement()
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getFileState() {
        return fileState;
    }

    public void setFileState(String fileState) {
        this.fileState = fileState;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public DateInfo dateInfoDateCreated() {
        return new DateInfo(dateCreated);
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getServerNodeUUID() {
        return serverNodeUUID;
    }

    public void setServerNodeUUID(String serverNodeUUID) {
        this.serverNodeUUID = serverNodeUUID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public DateInfo dateInfoExpirationDate() {
        return new DateInfo(expirationDate);
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getExecId() {
        return execId;
    }

    public void setExecId(Long execId) {
        this.execId = execId;
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
