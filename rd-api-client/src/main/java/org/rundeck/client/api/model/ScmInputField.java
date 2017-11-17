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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
public class ScmInputField {
    public String defaultValue;
    public String description;
    public String name;
    public Boolean required;
    public Map<String, String> renderingOptions;
    public String scope;
    public String title;
    public String type;
    public List<String> values;

    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("title", title);
        map.put("description", description);
        map.put("defaultValue", defaultValue);
        map.put("required", required);
        map.put("scope", scope);
        map.put("renderingOptions", renderingOptions);
        map.put("values", values);
        return map;
    }


}
