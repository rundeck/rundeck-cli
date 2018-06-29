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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author greg
 * @since 3/2/17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobFileItemList implements DataOutput {
    private Paging paging;
    private List<JobFileItem> files;

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public List<JobFileItem> getFiles() {
        return files;
    }

    @Override
    public List<?> asList() {
        return getFiles().stream().map(JobFileItem::asMap).collect(Collectors.toList());
    }

    public void setFiles(List<JobFileItem> files) {
        this.files = files;
    }
}
