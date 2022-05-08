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

import java.io.File;

@Data
public class JobFileOptions {

    @CommandLine.Option(names = {"-f", "--file"},
            description = "File path of the file to upload (load command) or destination for storing the jobs (list " +
                    "command)")
    File file;

    public boolean isFile() {
        return file != null;
    }

    @CommandLine.Option(names = {"-F", "--format"},
            defaultValue = "xml",

            description = "Format for the Job definition file, either xml or yaml")
    Format format;

    public enum Format {
        xml,
        yaml
    }

}
