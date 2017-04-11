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

/**
 * Created by greg on 5/20/16.
 */
public interface JobBaseOptions extends ProjectNameOptions {

    @Option(shortName = "f",
            longName = "file",
            description = "File path of the file to upload (load command) or destination for storing the jobs (list " +
                          "command)")
    File getFile();

    boolean isFile();

    @Option(shortName = "F",
            longName = "format",
            defaultValue = "xml",
            pattern = "^(xml|yaml)$",
            description = "Format for the Job definition file, either xml or yaml")
    String getFormat();


}
