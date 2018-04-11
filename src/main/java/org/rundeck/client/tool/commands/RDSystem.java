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

import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.HasSubCommands;
import org.rundeck.toolbelt.InputError;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.SystemInfo;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.system.ACLs;
import org.rundeck.client.tool.commands.system.Mode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * system subcommands
 */
@Command(description = "View system information", value = "system")
public class RDSystem extends AppCommand implements HasSubCommands {
    public RDSystem(final RdApp client) {
        super(client);
    }

    @Override
    public List<Object> getSubCommands() {
        return Arrays.asList(
                new ACLs(getRdApp()),
                new Mode(getRdApp())
        );
    }

    /**
     * Read system info
     */
    @Command(description = "Print system information and stats.")
    public void info(CommandOutput output) throws IOException, InputError {
        SystemInfo systemInfo = apiCall(RundeckApi::systemInfo);
        output.output(systemInfo.system.toMap());
    }

}
