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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by greg on 5/20/16.
 */
@Root(strict=false)
public class ImportResult {
    @ElementList()
    private List<JobLoadItem> succeeded;
    @ElementList()
    private List<JobLoadItem> failed;
    @ElementList()
    private List<JobLoadItem> skipped;

    public List<JobLoadItem> getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(List<JobLoadItem> succeeded) {
        this.succeeded = succeeded;
    }

    public List<JobLoadItem> getFailed() {
        return failed;
    }

    public void setFailed(List<JobLoadItem> failed) {
        this.failed = failed;
    }

    public List<JobLoadItem> getSkipped() {
        return skipped;
    }

    public void setSkipped(List<JobLoadItem> skipped) {
        this.skipped = skipped;
    }


}
