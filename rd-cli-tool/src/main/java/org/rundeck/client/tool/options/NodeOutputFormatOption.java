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

/**
 * @author greg
 * @since 11/22/16
 */
@Data
public class NodeOutputFormatOption extends VerboseOption implements OutputFormat {

    @CommandLine.Option(names = {"-%", "--outformat"},
            description = "Output format specifier for Node info. You can use \"%key\" where key is one of:" +
                    "nodename, hostname, osFamily, osVersion, osArch, description, username, tags, or any " +
                    "attribute. E.g. \"%nodename %tags\"")
    private String outputFormat;

}
