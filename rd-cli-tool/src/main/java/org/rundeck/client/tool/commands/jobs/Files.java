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

package org.rundeck.client.tool.commands.jobs;


import lombok.Data;
import org.rundeck.client.tool.extension.BaseCommand;
import picocli.CommandLine;
import org.rundeck.client.tool.extension.RdTool;


import org.rundeck.client.tool.InputError;
import okhttp3.RequestBody;
import org.rundeck.client.api.model.JobFileItem;
import org.rundeck.client.api.model.JobFileItemList;
import org.rundeck.client.api.model.JobFileUploadResult;
import org.rundeck.client.api.model.Paging;
import org.rundeck.client.tool.options.PagingResultOptions;
import org.rundeck.client.util.Client;

import java.io.File;
import java.io.IOException;

/**
 * @author greg
 * @since 3/1/17
 */
@CommandLine.Command(description = "List and manage File options for Jobs.", name = "files")
public class Files extends BaseCommand {


    @CommandLine.Command(description = "Get info about a Job input option file (API v19)")
    public void info(
            @CommandLine.Option(names = {"-i", "--id"}, description = "File ID")
            String id
    ) throws IOException, InputError {
        JobFileItem jobFileItem = apiCall(api -> api.getJobFileInfo(id));
        getRdOutput().output(jobFileItem);
    }

    @Data
    static class FileListOpts extends PagingResultOptions {
        @CommandLine.Option(names = {"-j", "--jobid"}, description = "Job ID")
        String jobId;

        boolean isJobId() {
            return jobId != null;
        }

        @CommandLine.Option(names = {"-e", "--eid"}, description = "Execution ID")
        String execId;

        boolean isExecId() {
            return execId != null;
        }

        @CommandLine.Option(names = {"-s", "--state"},
                description = "File state filter for listing Files for a Job only. (default:temp), one of: temp," +
                        "expired,deleted,retained.")
        FileState fileState;

        boolean isFileState() {
            return fileState != null;
        }
    }

    enum FileState {
        temp,
        expired,
        deleted,
        retained
    }

    @CommandLine.Command(description = "List files uploaded for a Job or Execution (API v19). Specify Job ID or Execution ID")
    public void list(@CommandLine.Mixin FileListOpts opts) throws IOException, InputError {

        if (!opts.isJobId() && !opts.isExecId() || opts.isExecId() && opts.isJobId()) {
            throw new InputError("One of -j/--jobid or -e/--eid is required");
        }
        if (opts.isExecId() && opts.isFileState()) {
            throw new InputError("-s/--state not a valid option for -e/--eid");
        }
        int offset = opts.isOffset() ? opts.getOffset() : 0;
        int max = opts.isMax() ? opts.getMax() : 20;

        JobFileItemList result;
        if (opts.isJobId()) {
            result = apiCall(api -> api.listJobFiles(
                    opts.getJobId(),
                    opts.isFileState() ? opts.getFileState().toString() : null,
                    offset,
                    max
            ));
        } else {
            result = apiCall(api -> api.listExecutionFiles(opts.getExecId(), offset, max));
        }

        Paging paging = result.getPaging();

        if (paging != null) {
            getRdOutput().info(paging);
        }

        getRdOutput().output(result.asList());


        if (paging != null && paging.hasMoreResults()) {
            getRdOutput().info(paging.moreResults("-o"));
        }
    }

    @Data
    static class FileUploadOpts {

        @CommandLine.Option(names = {"-i", "--id"}, description = "Job ID", required = true)
        String id;

        @CommandLine.Option(names = {"-o", "--option"}, description = "Option name", required = true)
        String option;

        @CommandLine.Option(names = {"-f", "--file"},
                description =
                        "File path of the file to upload (load command) or destination for storing the jobs (list " +
                                "command)", required = true)
        File file;
    }

    @CommandLine.Command(description = "Upload a file as input for a job option (API v19). Returns a unique key for the uploaded" +
            " file, which can be used as the option value when running the job.")
    public boolean load(@CommandLine.Mixin FileUploadOpts options) throws IOException, InputError {
        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new InputError(String.format("File is not readable or does not exist: %s", input));
        }

        String fileName = input.getName();
        JobFileUploadResult jobFileUploadResult = uploadFileForJob(
                getRdTool(),
                input,
                options.getId(),
                options.getOption()
        );

        String fileid = jobFileUploadResult.getFileIdForOption(options.getOption());
        if (null != fileid) {
            getRdOutput().info("File " + fileName + " uploaded successfully for option " + options.getOption());
            getRdOutput().info("File key:");
            getRdOutput().output(fileid);
            return true;
        } else {
            getRdOutput().error(String.format("Expected one option result for option %s, but saw: ", options.getOption()));
        }
        getRdOutput().output(jobFileUploadResult);
        return false;
    }

    /**
     * Upload a file for a job option input and return the result
     *
     */
    public static JobFileUploadResult uploadFileForJob(
            final RdTool rdTool,
            final File input,
            final String jobId,
            final String optionName
    ) throws IOException, InputError
    {
        if (invalidInputFile(input)) {
            throw new IOException("Can't read file: " + input);
        }
        RequestBody requestBody = RequestBody.create(Client.MEDIA_TYPE_OCTET_STREAM, input);
        return rdTool.apiCallDowngradable(
                api -> api.uploadJobOptionFile(
                        jobId,
                        optionName,
                        input.getName(),
                        requestBody
                )
        );
    }

    /**
     *
     * @return true if the file is invalid
     */
    public static boolean invalidInputFile(final File input) {
        return !input.exists() || !input.canRead() || !input.isFile();
    }
}
