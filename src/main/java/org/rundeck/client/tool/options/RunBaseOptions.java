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

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;
import org.rundeck.client.api.model.DateInfo;

import java.util.List;

@CommandLineInterface(application = "run")
public interface RunBaseOptions extends JobIdentOptions, FollowOptions, OptionalProjectOptions, NodeFilterOptions {
    @Option(shortName = "l",
            longName = "loglevel",
            description = "Run the command using the specified LEVEL. LEVEL can be verbose, info, warning, error.",
            defaultValue = {"info"},
            pattern = "(verbose|info|warning|error)")
    String getLoglevel();

    @Option(hidden = true, pattern = "(verbose|info|warning|error)")
    String getLogevel();

    boolean isLogevel();

    @Option(shortName = "u", longName = "user", description = "A username to run the job as, (runAs access required).")
    String getUser();

    boolean isUser();

    @Option(shortName = "@",
            longName = "at",
            description = "Run the job at the specified date/time. ISO8601 format (yyyy-MM-dd'T'HH:mm:ssXX)")
    DateInfo getRunAtDate();

    boolean isRunAtDate();

    @Option(shortName = "d",
            longName = "delay",
            description = "Run the job at a certain time from now. Format: ##[smhdwMY] where ## " +
                          "is an integer and the units are seconds, minutes, hours, days, weeks, Months, Years. Can combine " +
                          "units, e.g. \"2h30m\", \"20m30s\"",
            pattern = "(\\d+[smhdwMY]\\s*)+")
    String getRunDelay();

    boolean isRunDelay();

    @Unparsed(name = "-- -OPT VAL -OPT2 VAL -OPTFILE @filepath", description = "Job options")
    List<String> getCommandString();

}

