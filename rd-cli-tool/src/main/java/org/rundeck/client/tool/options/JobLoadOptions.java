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

import lombok.Data;
import picocli.CommandLine;

@Data
public class JobLoadOptions {

    @CommandLine.Option(names = {"-d", "--duplicate"},
            defaultValue = "update",
            description = "Behavior when uploading a Job matching a name+group that already exists, either: update, " +
                    "skip, create")
    Duplication duplicate = Duplication.update;

    public enum Duplication {
        update,
        skip,
        create
    }

    @CommandLine.Option(names = {"-r", "--remove-uuids"}, description = "Remove UUIDs when uploading")
    boolean removeUuids;


}
