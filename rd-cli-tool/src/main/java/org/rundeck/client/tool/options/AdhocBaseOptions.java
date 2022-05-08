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
import java.net.URL;
import java.util.List;

@Data
public class AdhocBaseOptions extends ProjectNameOptions {


    @CommandLine.Option(names = {"-C", "--threadcount"},
            description = "Execute using COUNT threads",
            defaultValue = "1")
    int threadcount = 1;

    @CommandLine.Option(names = {"-K", "--keepgoing"},
            description = "Keep going when an error occurs")
    boolean keepgoing;


    @CommandLine.Option(names = {"-s", "--script"}, description = "Dispatch specified script file")
    File scriptFile;


    @CommandLine.Option(names = {"-u", "--url"}, description = "Download a URL and dispatch it as a script")
    URL Url;

    @CommandLine.Option(names = {"-i", "--interpreter"}, description = "Script interpreter string")
    String ScriptInterpreter;

    @CommandLine.Option(names = {"-Q", "--quoted"}, description = "Use quoted args")
    boolean argsQuoted;

    @CommandLine.Option(names = {"-x", "--extension"}, description = "File extension to use for temporary script")
    String FileExtension;

    @CommandLine.Option(names = {"-S", "--stdin"}, description = "Execute input read from STDIN")
    boolean stdin;

    @CommandLine.Parameters(paramLabel = "COMMAND", description = "Dispatch specified command string")
    List<String> CommandString;

}
