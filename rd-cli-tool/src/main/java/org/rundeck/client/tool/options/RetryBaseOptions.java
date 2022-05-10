/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
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
public class RetryBaseOptions extends JobIdentOptions implements  OutputFormat {
    @CommandLine.Option(names = {"-l", "--loglevel"},
            description = "Run the command using the specified LEVEL. LEVEL can be verbose, info, warning, error.",
            defaultValue = "info")
    private RunBaseOptions.Loglevel loglevel;

    @CommandLine.Option(names = {"-e", "--eid"}, description = "Execution ID to retry on failed nodes.")
    String eid;


    boolean isLoglevel() {
        return loglevel != null;
    }


    @CommandLine.Option(names = {"-u", "--user"}, description = "A username to run the job as, (runAs access required).")
    private String user;

    boolean isUser() {
        return user != null;
    }

    @CommandLine.Option(
            names = {"-F", "--failedNodes"},
            description = "Run only on failed nodes (default=true).",
            defaultValue = "true",
            arity = "1"
    )
    private boolean failedNodes;

    @CommandLine.Option(names = {"--raw"},
            description = "Treat option values as raw text, so that '-opt @value' is sent literally")
    private boolean rawOptions;

    @CommandLine.Parameters(paramLabel = "-OPT VAL or -OPTFILE @filepath", description = "Job options")
    private List<String> commandString;

    @CommandLine.Option(names = {"-%", "--outformat"},
            description = "Output format specifier for execution logs. You can use \"%%key\" where key is one of:" +
                    "time,level,log,user,command,node. E.g. \"%%user@%%node/%%level: %%log\"")
    private String outputFormat;


    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Extended verbose output")
    private boolean verbose;
}

