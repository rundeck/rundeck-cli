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

/**
 * @author greg
 * @since 4/4/17
 */
@Getter
@Setter
public class JobIdentOptions extends ProjectNameOptions {

    @CommandLine.Option(names = {"-j", "--job"}, description = "Job identified by name and group: 'group/name'. (Project name required)")
    String job;

    public boolean isJob() {
        return job != null;
    }

    @CommandLine.Option(names = {"-i", "--id"}, description = "Run the Job with this IDENTIFIER")
    String id;

    public boolean isId() {
        return id != null;
    }

}
