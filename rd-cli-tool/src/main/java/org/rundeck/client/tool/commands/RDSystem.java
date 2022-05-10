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

package org.rundeck.client.tool.commands;

import org.rundeck.client.tool.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.tool.commands.system.ACLs;
import org.rundeck.client.tool.commands.system.Mode;
import org.rundeck.client.tool.extension.BaseCommand;
import picocli.CommandLine;

import java.io.IOException;

/**
 * system subcommands
 */
@CommandLine.Command(description = "View system information", name = "system",
        subcommands = {
                ACLs.class,
                Mode.class
        })
public class RDSystem extends BaseCommand {

    /**
     * Read system info
     */
    @CommandLine.Command(description = "Print system information and stats.")
    public void info() throws IOException, InputError {
        SystemInfo systemInfo = apiCall(RundeckApi::systemInfo);
        getRdOutput().output(systemInfo.system.toMap());
    }
}
