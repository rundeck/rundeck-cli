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
import com.lexicalscope.jewel.cli.Unparsed;

import java.io.File;
import java.net.URL;
import java.util.List;

public interface AdhocBaseOptions extends ProjectNameOptions, FollowOptions, NodeFilterOptions {


    @Option(shortName = "C",
            longName = "threadcount",
            description = "Execute using COUNT threads",
            defaultValue = {"1"})
    int getThreadcount();

    boolean isThreadcount();

    @Option(shortName = "K",
            longName = "keepgoing",
            description = "Keep going when an error occurs")
    boolean isKeepgoing();


    @Option(shortName = "s", longName = "script", description = "Dispatch specified script file")
    File getScriptFile();

    boolean isScriptFile();


    @Option(shortName = "u", longName = "url", description = "Download a URL and dispatch it as a script")
    URL getUrl();

    boolean isUrl();

    @Option(shortName = "i", longName = "interpreter", description = "Script interpreter string")
    String getScriptInterpreter();

    boolean isScriptInterpreter();

    @Option(shortName = "Q", longName = "quoted", description = "Use quoted args")
    boolean isArgsQuoted();

    @Option(shortName = "x", longName = "extension", description = "File extension to use for temporary script")
    String getFileExtension();

    boolean isFileExtension();

    @Option(shortName = "S", longName = "stdin", description = "Execute input read from STDIN")
    boolean isStdin();

    @Unparsed(name = "-- COMMAND", description = "Dispatch specified command string")
    List<String> getCommandString();


}
