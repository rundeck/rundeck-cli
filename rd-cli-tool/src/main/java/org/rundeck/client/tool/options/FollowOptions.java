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

@Getter @Setter
public class FollowOptions extends RunOptions {

    @CommandLine.Option(names = {"-q", "--quiet"}, description = "Echo no output. Combine with -f/--follow to wait silently until the execution completes. Useful for non-interactive scripts.")
    boolean quiet;

    @CommandLine.Option(names = {"-r", "--progress"},
            description = "Do not echo log text, just an indicator that output is being received.")
    boolean progress;

    @CommandLine.Option(names = {"-t", "--restart"}, description = "Restart from the beginning")
    boolean restart;

    @CommandLine.Option(names = {"-T", "--tail"},
            defaultValue = "1",
            description = "Number of lines to tail from the end, default: 1")
    long tail;

}
