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
import java.util.Map;

/**
 * @author greg
 * @since 12/13/16
 */
public class ScmPlugin {
    public String type;
    public String title;
    public String description;
    public Boolean configured;
    public Boolean enabled;

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("title", title);
        map.put("description", description);
        map.put("configured", configured);
        map.put("enabled", enabled);
        return map;
    }
}
