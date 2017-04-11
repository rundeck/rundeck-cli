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

import com.lexicalscope.jewel.cli.Option;

import java.io.File;

public interface JobListOptions extends JobBaseOptions{


    @Option(shortName = "j", longName = "job", description = "Job name filter")
    String getJob();

    boolean isJob();

    @Option(shortName = "g", longName = "group", description = "Job Group filter")
    String getGroup();

    boolean isGroup();

    @Option(shortName = "J", longName = "jobxact", description = "Exact Job name")
    String getJobExact();

    boolean isJobExact();

    @Option(shortName = "G", longName = "groupxact", description = "Exact Job Group")
    String getGroupExact();

    boolean isGroupExact();

    @Option(shortName = "i", longName = "idlist", description = "Comma separated list of Job IDs")
    String getIdlist();

    boolean isIdlist();

}
