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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author greg
 * @since 4/10/17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectImportStatus {
    public Boolean successful;

    @JsonProperty("import_status")
    public String importStatus;
    public List<String> errors;
    @JsonProperty("execution_errors")
    public List<String> executionErrors;
    @JsonProperty("acl_errors")
    public List<String> aclErrors;

    public boolean getResultSuccess() {
        return null != successful ? successful : null != importStatus && "successful".equals(importStatus);
    }
}
