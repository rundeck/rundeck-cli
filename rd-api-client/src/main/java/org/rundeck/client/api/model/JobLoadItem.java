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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement()
public class JobLoadItem extends JobItem {

    private String error;

    @XmlElement()
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toBasicString() {
        if (null != error) {
            return String.format(
                    "[%s] %s%s\n\t:%s",
                    getId() != null ? getId() : "id:?",
                    getGroup() != null ? getGroup() + "/" : "",
                    getName() != null ? getName() : "(missing name)",
                    getError()
            );
        } else {
            return super.toBasicString();
        }
    }

    @Override
    public Map<?, ?> asMap() {
        HashMap<Object, Object> map = new HashMap<>(toMap());
        if (null != error) {
            map.put("error", getError());
        }
        return map;
    }
}
