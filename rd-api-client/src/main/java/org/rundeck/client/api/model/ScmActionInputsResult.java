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
import org.rundeck.toolbelt.Formatable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmActionInputsResult  {
    public String title;
    public String description;
    public String integration;
    public String actionId;
    public List<ScmInputField> fields;
    public List<ScmImportItem> importItems;
    public List<ScmExportItem> exportItems;

    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("integration", integration);
        map.put("actionId", actionId);
        if (null != fields) {
            map.put("fields", fields);
        }
        if (null != importItems) {
            map.put("items", importItems);
        }
        if (null != exportItems) {
            map.put("items", exportItems);
        }

        return map;
    }
}
