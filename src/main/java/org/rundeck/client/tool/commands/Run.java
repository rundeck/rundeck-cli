package org.rundeck.client.tool.commands;

import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import org.rundeck.client.api.model.Execution;
import org.rundeck.client.api.model.JobFileUploadResult;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.JobRun;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.jobs.Files;
import org.rundeck.client.tool.options.RunBaseOptions;
import org.rundeck.client.util.Format;
import org.rundeck.client.util.Quoting;
import retrofit2.Call;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by greg on 5/20/16.
 */
@Command(description = "Run a Job. Specify option arguments after -- as \"-opt value\". Upload files as \"-opt " +
                       "@path\". Note: For literal '@' in option value that starts with @, use @@.")
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
        String jobId;
        if (options.isJob()) {
            if (!options.isProject()) {
                throw new InputError("-p project is required with -j");
            }
            String job = options.getJob();
            String[] parts = Jobs.splitJobNameParts(job);
            String project = projectOrEnv(options);
            List<JobItem> jobItems = apiCall(api -> api.listJobs(
                    project,
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
                return false;
            }
            JobItem jobItem = jobItems.get(0);
            out.info(String.format("Found matching job: %s%n", jobItem.toBasicString()));
            jobId = jobItem.getId();
        } else if (options.isId()) {
            jobId = options.getId();
        } else {
            throw new InputError("-j job or -i id is required");

        }
        Call<Execution> executionListCall;
        Date runat = null;
        if (getClient().getApiVersion() >= 18) {
            JobRun request = new JobRun();
            request.setLoglevel(options.getLoglevel());
            request.setFilter(options.getFilter());
            request.setAsUser(options.getUser());
            List<String> commandString = options.getCommandString();
            Map<String, String> jobopts = new HashMap<>();
            Map<String, File> fileinputs = new HashMap<>();
            String key = null;
            if (null != commandString) {
                for (String s : commandString) {
                    if (key == null && s.startsWith("-")) {
                        key = s.substring(1);
                    } else if (key != null) {
                        if (s.charAt(0) == '@') {
                            //file input
                            if (s.length() > 1 && s.charAt(1) != '@') {
                                File file = new File(s.substring(1));
                                fileinputs.put(key, file);
                            } else {
                                //replace escaped @ signs
                                s = s.replaceAll(Pattern.quote("@@"), Matcher.quoteReplacement("@"));
                            }
                        }
                        jobopts.put(key, s);
                        key = null;
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
                    if (!Files.validInputFile(file)) {
                        throw new InputError("File Option -" + optionName + ": File cannot be read: " + file);
                    }
                }
                for (String optionName : fileinputs.keySet()) {
                    File file = fileinputs.get(optionName);
                    JobFileUploadResult jobFileUploadResult = Files.uploadFileForJob(
                            getClient(),
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
            executionListCall = getClient().getService().runJob(jobId, request);
        } else {
            executionListCall = getClient().getService().runJob(
                    jobId,
                    Quoting.joinStringQuoted(options.getCommandString()),
                    options.getLoglevel(),
                    options.getFilter(),
                    options.getUser()
            );
        }
        Execution execution = getClient().checkError(executionListCall);
        String started = runat != null ? "scheduled" : "started";
        out.info(String.format("Execution %s: %s%n", started, execution.toBasicString()));

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
        return Executions.maybeFollow(getClient(), options, execution.getId(), out);
    }

    private Date parseDelayTime(final String delayString) throws InputError {
        long delayms = System.currentTimeMillis();
        Pattern p = Pattern.compile("(?<digits>\\d+)(?<unit>[smhdwMY])\\s*");
        Matcher matcher = p.matcher(delayString);
        int months = 0;
        int years = 0;
        while (matcher.find()) {
            String digit = matcher.group("digits");
            String unit = matcher.group("unit");
            long count = Integer.parseInt(digit);
            long unitms = 0;
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
