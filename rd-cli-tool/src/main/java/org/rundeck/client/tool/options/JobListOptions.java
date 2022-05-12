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

package org.rundeck.client.tool.options;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

import java.util.List;

@Getter @Setter
public class JobListOptions extends ProjectNameOptions {


    @CommandLine.Option(names = {"-j", "--job"}, description = "Job name filter")
    String job;

    public boolean isJob() {
        return job != null;
    }


    @CommandLine.Option(names = {"-g", "--group"}, description = "Job Group filter")
    String group;

    public boolean isGroup() {
        return group != null;
    }


    @CommandLine.Option(names = {"-J", "--jobxact"}, description = "Exact Job name")
    String jobExact;

    public boolean isJobExact() {
        return jobExact != null;
    }

    @CommandLine.Option(names = {"-G", "--groupxact"}, description = "Exact Job Group")
    String groupExact;

    public boolean isGroupExact() {
        return groupExact != null;
    }

    @CommandLine.Option(names = {"-i", "--idlist"}, description = "Comma separated list of Job IDs",
            arity = "1..*",
            split = "\\s*,\\s*")
    List<String> idlist;

    public boolean isIdlist() {
        return idlist != null && idlist.size() > 0;
    }

}
