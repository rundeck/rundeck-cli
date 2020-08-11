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

import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobFileUploadResult;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.JobRun;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.jobs.Files;
import org.rundeck.client.tool.options.JobIdentOptions;
import org.rundeck.client.tool.options.RunBaseOptions;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.Quoting;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
 * run subcommand
 */
@Command(description = "Run a Job. Specify option arguments after -- as \"-opt value\". Upload files as \"-opt " +
                       "@path\" or \"-opt@ path\".")
public class Run extends AppCommand {

    public static final int SEC_MS = 1000;
    public static final int MIN_MS = 60 * 1000;
    public static final int HOUR_MS = 60 * 60 * 1000;
    public static final int DAY_MS = 24 * 60 * 60 * 1000;
    public static final int WEEK_MS = 7 * 24 * 60 * 60 * 1000;

    public Run(final RdApp client) {
        super(client);
    }

    @Command(isDefault = true, isSolo = true)
    public boolean run(RunBaseOptions options, CommandOutput out) throws IOException, InputError {
        String jobId = getJobIdFromOpts(options, out, this, () -> projectOrEnv(options));
        if (null == jobId) {
            return false;
        }
        Execution execution;
        Date runat = null;

        final String loglevel;
        if (options.isLogevel()) {
            out.warning("--logevel is [DEPRECATED: To be removed], use --loglevel");
            loglevel = options.getLogevel().toUpperCase();
        } else {
            loglevel = null != options.getLoglevel() ? options.getLoglevel().toUpperCase() : null;
        }

        if (getClient().getApiVersion() >= 18) {
            JobRun request = new JobRun();
            request.setLoglevel(loglevel);
            request.setFilter(options.getFilter());
            request.setAsUser(options.getUser());
            List<String> commandString = options.getCommandString();
            boolean rawOptions = options.isRawOptions();
            Map<String, String> jobopts = new HashMap<>();
            Map<String, File> fileinputs = new HashMap<>();
            String key = null;
            if (null != commandString) {
                boolean isfile = false;
                for (String part : commandString) {
                    if (key == null && part.startsWith("-")) {
                        key = part.substring(1);
                        if (key.endsWith("@")) {
                            key = key.substring(0, key.length() - 1);
                            isfile = true;
                        }
                    } else if (key != null) {
                        String filepath = null;
                        if (!rawOptions && part.charAt(0) == '@' && !isfile) {
                            //file input
                            filepath = part.substring(1);
                            isfile = true;
                        }
                        if (isfile) {
                            File file = new File(filepath != null ? filepath : part);
                            fileinputs.put(key, file);
                        }
                        jobopts.put(key, part);
                        key = null;
                        isfile = false;
                    }
                }
            }
            if (key != null) {
                throw new InputError(
                        String.format(
                                "Incorrect job options, expected: \"-%s value\", but saw only \"-%s\"",
                                key,
                                key
                        ));
            }
            if (fileinputs.size() > 0 && getClient().getApiVersion() < 19) {
                out.warning(
                        String.format(
                                "APIv19 is required for option file inputs (using %d). The option values will be used" +
                                " verbatim.",
                                getClient().getApiVersion()
                        ));
            } else if (fileinputs.size() > 0) {
                for (String optionName : fileinputs.keySet()) {
                    File file = fileinputs.get(optionName);
                    if (Files.invalidInputFile(file)) {
                        throw new InputError("File Option -" + optionName + ": File cannot be read: " + file);
                    }
                }
                for (String optionName : fileinputs.keySet()) {
                    File file = fileinputs.get(optionName);
                    JobFileUploadResult jobFileUploadResult = Files.uploadFileForJob(
                            this,
                            file,
                            jobId,
                            optionName
                    );
                    String fileid = jobFileUploadResult.getFileIdForOption(optionName);
                    jobopts.put(optionName, fileid);
                    out.info(String.format("File Upload OK (%s -> %s)", file, fileid));
                }
            }

            request.setOptions(jobopts);
            if (options.isRunAtDate()) {
                try {
                    runat = options.getRunAtDate().toDate("yyyy-MM-dd'T'HH:mm:ssXX");
                    request.setRunAtTime(runat);
                } catch (ParseException e) {
                    throw new InputError("-@/--at date format is not valid", e);
                }
            } else if (options.isRunDelay()) {
                runat = parseDelayTime(options.getRunDelay());
                request.setRunAtTime(runat);
                out.info(String.format(
                        "Scheduling execution in %s, at: %s",
                        options.getRunDelay(),
                        Format.date(runat, "yyyy-MM-dd'T'HH:mm:ssXX")
                ));
            }
            execution = apiCall(api -> api.runJob(jobId, request));
        } else {
            execution = apiCall(api -> api.runJob(
                    jobId,
                    Quoting.joinStringQuoted(options.getCommandString()),
                    loglevel,
                    options.getFilter(),
                    options.getUser()
            ));
        }
        String started = runat != null ? "scheduled" : "started";

        if(!options.isFollow()){
            if (!options.isOutputFormat() && !options.isVerbose()) {
                out.info(String.format("Execution %s%n", started));
            }
            Executions.outputExecutionList(options, out, getAppConfig(), Stream.<Execution>builder().add(execution).build());
        }else{
            out.info(String.format("Execution %s: %s%n", started, execution.toBasicString()));
        }

        if (runat != null && options.isFollow()) {
            Date now = new Date();
            long diff = runat.getTime() - now.getTime();
            out.info(String.format("Waiting until scheduled execution starts...(in %dms)", diff));
            while (now.compareTo(runat) < 0) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
                now = new Date();
            }
            out.info("Started.");
        }
        return Executions.maybeFollow(this, options, execution.getId(), out);
    }

    /**
     * If job ID is supplied, use that, otherwise query for matching project/jobname and return found ID, or null if not
     * found
     *
     * @param options ident options
     * @param out     output
     * @param rdTool  rdTool
     * @param project project name, or null
     * @return job ID or null
     */
    public static String getJobIdFromOpts(
            final JobIdentOptions options,
            final CommandOutput out,
            final RdTool rdTool,
            final GetInput<String> project
    )
            throws InputError, IOException
    {
        if (options.isId()) {
            return options.getId();
        }
        if (!options.isJob()) {
            throw new InputError("-j job or -i id is required");
        }
        String proj = project.get();
        String job = options.getJob();
        String[] parts = Jobs.splitJobNameParts(job);
        List<JobItem> jobItems = rdTool.apiCallDowngradable(api -> api.listJobs(
                proj,
                null,
                null,
                parts[1],
                parts[0]
        ));
        if (jobItems.size() != 1) {
            out.error(String.format("Could not find a unique job with name: %s%n", job));
            if (jobItems.size() > 0) {

                out.error(String.format("Found %d matching jobs:%n", jobItems.size()));
                for (JobItem jobItem : jobItems) {
                    out.error(String.format("* %s%n", jobItem.toBasicString()));

                }
            } else {
                out.error("Found 0 matching jobs.");
            }
            return null;
        } else {
            JobItem jobItem = jobItems.get(0);
            out.info(String.format("Found matching job: %s%n", jobItem.toBasicString()));
            return jobItem.getId();
        }
    }

    private Date parseDelayTime(final String delayString) {
        long delayms = System.currentTimeMillis();
        Pattern p = Pattern.compile("(?<digits>\\d+)(?<unit>[smhdwMY])\\s*");
        Matcher matcher = p.matcher(delayString);
        int months = 0;
        int years = 0;
        while (matcher.find()) {
            String digit = matcher.group("digits");
            String unit = matcher.group("unit");
            long count = Integer.parseInt(digit);
            long unitms;
            //simple addition for time units
            switch (unit) {
                case "s":
                    unitms = SEC_MS;
                    break;
                case "m":
                    unitms = MIN_MS;
                    break;
                case "h":
                    unitms = HOUR_MS;
                    break;
                case "d":
                    unitms = DAY_MS;
                    break;
                case "w":
                    unitms = WEEK_MS;
                    break;
                default:
                    unitms = 0;
            }
            if ("M".equals(unit)) {
                months += count;
            } else if ("Y".equals(unit)) {
                years += count;
            }
            delayms += (count * unitms);
        }
        Date date = new Date(delayms);
        if (months > 0 || years > 0) {
            //use calendar for date units
            GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getDefault());
            gregorianCalendar.setTime(date);
            if (months > 0) {
                gregorianCalendar.add(Calendar.MONTH, months);
            }
            if (years > 0) {
                gregorianCalendar.add(Calendar.YEAR, years);
            }
            date = gregorianCalendar.getTime();
        }
        return date;
    }
}
