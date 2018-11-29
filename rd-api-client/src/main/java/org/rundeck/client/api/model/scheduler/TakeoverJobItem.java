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

package org.rundeck.client.api.model.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rundeck.client.api.model.JobItem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TakeoverJobItem extends JobItem {
    private String previousOwner;

    public String getPreviousOwner() {
        return previousOwner;
    }

    public TakeoverJobItem setPreviousOwner(String previousOwner) {
        this.previousOwner = previousOwner;
        return this;
    }


    @Override
    public String toString() {
        return super.toBasicString() +
            "\n  previousOwner='" + previousOwner + '\'';
    }
}
