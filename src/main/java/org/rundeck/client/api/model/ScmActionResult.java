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

import java.util.Map;

/**
 * Result of <a href="http://rundeck.org/docs/api/index.html#setup-scm-plugin-for-a-project">Setup SCM Plugin for a
 * Project</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmActionResult {
    public String message;
    public String nextAction;
    public boolean success;
    public Map<String, String> validationErrors;

    public Map<?, ?> toMap() {
        return validationErrors;
    }
}
