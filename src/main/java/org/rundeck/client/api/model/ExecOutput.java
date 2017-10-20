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

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecOutput {
    public String id;
    public String message;
    public String error;
    public boolean unmodified;
    public boolean empty;
    public long offset;
    public boolean completed;
    public boolean execCompleted;
    public boolean hasFailedNodes;
    public String execState;
    public long lastModified;
    public long execDuration;
    public float percentLoaded;
    public int totalSize;
    public List<ExecLog> entries;
    public Boolean compacted;
    public String compactedAttr;

    private List<ExecLog> decompacted;

    public List<ExecLog> decompactEntries() {
        if (null == compacted || !compacted) {
            return entries;
        }
        if (null != decompacted) {
            return decompacted;
        }
        ExecLog prev = null;
        ArrayList<ExecLog> newentries = new ArrayList<>();

        for (ExecLog entry : entries) {
            ExecLog clone = entry.decompact(prev);
            newentries.add(clone);
            prev = clone;
        }
        decompacted = newentries;
        return newentries;
    }

}
