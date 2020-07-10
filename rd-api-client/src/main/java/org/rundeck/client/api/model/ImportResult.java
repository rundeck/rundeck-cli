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


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement()
public class ImportResult {
    private List<JobLoadItem> succeeded;
    private List<JobLoadItem> failed;
    private List<JobLoadItem> skipped;

    @XmlElementWrapper
    @XmlElement(name = "job")
    public List<JobLoadItem> getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(List<JobLoadItem> succeeded) {
        this.succeeded = succeeded;
    }

    @XmlElementWrapper
    @XmlElement(name = "job")
    public List<JobLoadItem> getFailed() {
        return failed;
    }

    public void setFailed(List<JobLoadItem> failed) {
        this.failed = failed;
    }

    @XmlElementWrapper
    @XmlElement(name = "job")
    public List<JobLoadItem> getSkipped() {
        return skipped;
    }

    public void setSkipped(List<JobLoadItem> skipped) {
        this.skipped = skipped;
    }


}
