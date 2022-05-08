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
import org.rundeck.client.tool.ProjectInput;

import java.util.regex.Pattern;

/**
 * Required project name option
 *
 * @author greg
 * @since 4/10/17
 */
@Data
public class ProjectRequiredNameOptions implements ProjectInput {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec; // injected by picocli
    @CommandLine.Option(
            names = {"-p", "--project"},
            description = "Project name",
            required = true
    )
    private String project;

    void validate() {
        validateProjectName(getProject(), spec);
    }

    public static void validateProjectName(String project, CommandLine.Model.CommandSpec spec) {
        Pattern pat = Pattern.compile(PROJECT_NAME_PATTERN);
        if (!pat.matcher(project).matches()) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "Invalid option: --project/-p does not match: " + PROJECT_NAME_PATTERN
            );
        }
    }
}
