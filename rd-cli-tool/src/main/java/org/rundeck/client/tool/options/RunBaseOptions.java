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

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import org.rundeck.client.api.model.DateInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public class RunBaseOptions extends JobIdentOptions  {
    @CommandLine.Option(names = {"-l", "--loglevel"},
            description = "Run the command using the specified LEVEL. LEVEL can be debug, verbose, info, warning, error.",
            defaultValue = "info")
    private Loglevel loglevel;

    public static enum Loglevel {
        debug,
        verbose,
        info,
        warning,
        error
    }

    @CommandLine.Option(names = {"-u", "--user"}, description = "A username to run the job as, (runAs access required).")
    private String user;

    public boolean isUser() {
        return user != null;
    }

    //XXX: test for picocli
    @CommandLine.Option(names = {"-@", "--at"},
            description = "Run the job at the specified date/time. ISO8601 format (yyyy-MM-dd'T'HH:mm:ssXX)")
    private DateInfo runAtDate;

    public boolean isRunAtDate() {
        return runAtDate != null;
    }

    @CommandLine.Option(names = {"-d", "--delay"},
            description = "Run the job at a certain time from now. Format: ##[smhdwMY] where ## " +
                    "is an integer and the units are seconds, minutes, hours, days, weeks, Months, Years. Can combine " +
                    "units, e.g. \"2h30m\", \"20m30s\""
            )
    private String runDelay;
    public static Pattern RUN_DELAY_PATTERN = Pattern.compile("(\\d+[smhdwMY]\\s*)+");

    public boolean isRunDelay() {
        return runDelay != null;
    }

    @CommandLine.Option(names = {"--raw"},
            description = "Treat option values as raw text, so that '-opt @value' is sent literally")
    private boolean rawOptions;

    @CommandLine.Parameters(paramLabel = "-OPT VAL or -OPTFILE @filepath", description = "Job options")
    private List<String> commandString;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;
    public void validate(){
        if(isRunDelay()){
            Matcher m=RUN_DELAY_PATTERN.matcher(getRunDelay());
            if(!m.matches()){
                throw new CommandLine.ParameterException(spec.commandLine(), "-d/--delay is not valid: " + getRunDelay() + ", must match: " + RUN_DELAY_PATTERN);
            }
        }
    }

}

