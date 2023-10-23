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

package org.rundeck.client.tool.commands.projects;

import lombok.Setter;
import lombok.Getter;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectExportStatus;
import org.rundeck.client.api.model.ProjectImportStatus;
import org.rundeck.client.tool.CommandOutput;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.Main;
import org.rundeck.client.tool.ProjectInput;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.ProjectRequiredNameOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.client.util.Util;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * @author greg
 * @since 4/10/17
 */

@CommandLine.Command(description = "Project Archives import and export", name = "archives")
public class Archives extends BaseCommand  {

    @Getter @Setter static class BaseOptions extends ProjectRequiredNameOptions{
        @CommandLine.Option(names = {"-f", "--file"}, description = "Output/Import file path", required = true)
        @Getter
        private File file;
    }

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec; // injected by picocli

    String validate(ProjectInput opts) throws InputError {
        if (null != opts.getProject()) {
            ProjectRequiredNameOptions.validateProjectName(opts.getProject(), spec);
        }
        return getRdTool().projectOrEnv(opts);
    }

    @Getter @Setter
    static class ArchiveImportOpts extends BaseOptions{
        @CommandLine.Option(names = {"-r"}, description = "Remove Job UUIDs in imported jobs. Default: preserve job UUIDs.")
        boolean remove;

        @CommandLine.Option(names = {"-x"},
                description = "Do not include executions in import. Default: do include executions in import.")
        boolean noExecutions;

        @CommandLine.Option(names = {"-c", "--include-config"},
                description = "Include project configuration in import, default: false")
        boolean includeConfig;

        @CommandLine.Option(names = {"-a", "--include-acl"}, description = "Include ACLs in import, default: false")
        boolean includeAcl;

        @CommandLine.Option(names = {"-s", "--include-scm"}, description = "Include SCM configuration in import, default: false (api v28 required)")
        boolean includeScm;

        @CommandLine.Option(names = {"-w", "--include-webhooks"}, description = "Include Webhooks in import, default: false (api v34 required)")
        boolean includeWebhooks;

        @CommandLine.Option(names = {"-t", "--regenerate-tokens"}, description = "regenerate the auth tokens associated with the webhook in import, default: false (api v34 required)")
        boolean whkRegenAuthTokens;

        @CommandLine.Option(names = {"-R", "--remove-webhooks-uuids"}, description = "Remove Webhooks UUIDs in import. Default: preserve webhooks UUIDs. (api v34 required)")
        boolean whkRegenUuid;

        @CommandLine.Option(names = {"-n", "--include-node-sources"}, description = "Include node resources in import, default: false (api v38 required)")
        boolean includeNodeSources;

        @CommandLine.Option(names = {"-i", "--async-import-enabled"}, description = "Enables asynchronous import process for the uploaded project file.")
        boolean asyncImportEnabled;

        @CommandLine.Option(
                names = {"--strict"},
                description = "Return non-zero exit status if any imported item had an error. Default: only job " +
                        "import errors are treated as failures.")
        boolean strict;


        @CommandLine.Option(
                names = {"--component", "-I"},
                arity = "0..*",
                description = "Enable named import components, such as tours-manager (enterprise). See <https://docs.rundeck.com/docs/api/rundeck-api.html#project-archive-import>")
        Set<String> components;

        @CommandLine.Option(
                names = {"--options", "-O"},
                arity = "0..*",
                description = "Set options for enabled components, in the form name.key=value")
        Map<String, String> componentOptions;

    }

    @CommandLine.Command(description = "Import a project archive", name = "import")
    public int importArchive(@CommandLine.Mixin ArchiveImportOpts opts) throws InputError, IOException {
        File input = opts.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new InputError(String.format("File is not readable or does not exist: %s", input));
        }
        if ((opts.isIncludeWebhooks() || opts.isWhkRegenAuthTokens()) && getRdTool().getClient().getApiVersion() < 34) {
            throw new InputError(String.format("Cannot use --include-webhooks or --regenerate-tokens with API < 34 (currently: %s)", getRdTool().getClient().getApiVersion()));
        }
        if ((opts.isIncludeWebhooks() || opts.isWhkRegenUuid()) && getRdTool().getClient().getApiVersion() < 34) {
            throw new InputError(String.format("Cannot use --include-webhooks or --remove-webhooks-uuids with API < 34 (currently: %s)", getRdTool().getClient().getApiVersion()));
        }
        if ((opts.isIncludeNodeSources()) && getRdTool().getClient().getApiVersion() < 38) {
            throw new InputError(String.format("Cannot use --include-node-sources with API < 38 (currently: %s)", getRdTool().getClient().getApiVersion()));
        }
        RequestBody body = RequestBody.create(input, Client.MEDIA_TYPE_ZIP);

        Map<String, String> extraCompOpts = new HashMap<>();
        if (opts.components != null && opts.components.size() > 0) {
            for (String component : opts.components) {
                extraCompOpts.put("importComponents." + component, "true");
            }
        }
        if (opts.componentOptions != null && opts.componentOptions.size() > 0) {
            for (Map.Entry<String, String> stringStringEntry : opts.componentOptions.entrySet()) {
                extraCompOpts.put("importOpts." + stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }
        String project = validate(opts);
        ProjectImportStatus status = apiCall(api -> api.importProjectArchive(
                project,
                opts.isRemove() ? "remove" : "preserve",
                !opts.isNoExecutions(),
                opts.isIncludeConfig(),
                opts.isIncludeAcl(),
                opts.isIncludeScm(),
                opts.isIncludeWebhooks(),
                opts.isWhkRegenAuthTokens(),
                opts.isWhkRegenUuid(),
                opts.isIncludeNodeSources(),
                opts.isAsyncImportEnabled(),
                extraCompOpts,
                body
        ));
        boolean anyerror = false;
        if (status.getResultSuccess()) {
            if( opts.isAsyncImportEnabled() ){

                String RD_URL =  "<RD_URL>";
                String projectPlaceholder = "<project>";
                String apiVersionPlaceholder = "<api_version>";

                getRdOutput().info("Asynchronous import operation started, please check status endpoint for more info.");
                getRdOutput().info("Users could check import status through endpoint: " + RD_URL + "/api/" + apiVersionPlaceholder + "/project/" + projectPlaceholder + "/async/import-status");
            }else{
                getRdOutput().info("Jobs imported successfully");
            }
        } else {
            anyerror = true;
            if (null != status.errors && status.errors.size() > 0) {
                getRdOutput().error("Some imported Jobs failed:");
                getRdOutput().error(status.errors);
            }
        }
        if (null != status.executionErrors && status.executionErrors.size() > 0) {
            anyerror = true;
            getRdOutput().error("Some imported executions failed:");
            getRdOutput().error(status.executionErrors);
        }
        if (null != status.aclErrors && status.aclErrors.size() > 0) {
            anyerror = true;
            getRdOutput().error("Some imported ACLs failed:");
            getRdOutput().error(status.aclErrors);
        }

        return (opts.isStrict() ? !anyerror : status.getResultSuccess()) ? 0 : 1;
    }


    @Getter @Setter
    static class ArchiveExportOpts extends BaseOptions {

        @CommandLine.Option(
                names = {"--execids", "-e"},
                description = "List of execution IDs. Exports only those ids.")
        List<String> executionIds;

        boolean isExecutionIds() {
            return executionIds != null && !executionIds.isEmpty();
        }


        @CommandLine.Option(
                names = {"--include", "-i"},
                description =
                        "List of archive contents to include. [all,jobs,executions,configs,readmes,acls,scm]. Default: " +
                                "all. (API v19 required for other " +
                                "options).")
        Set<Flags> includeFlags;

        boolean isIncludeFlags() {
            return includeFlags != null && !includeFlags.isEmpty();
        }

    }

    enum Flags {
        all,
        jobs,
        executions,
        configs,
        readmes,
        acls,
        scm
    }

    @CommandLine.Command(description = "Export a project archive")
    public int export(@CommandLine.Mixin ArchiveExportOpts opts) throws IOException, InputError {
        if (opts.isIncludeFlags() && opts.isExecutionIds()) {
            throw new InputError("Cannot use --execids/-e with --include/-i");
        }
        boolean apiv19 = getRdTool().getClient().getApiVersion() >= 19;

        Set<Flags> includeFlags = opts.isIncludeFlags() ? opts.getIncludeFlags() : new HashSet<>();
        if (!opts.isIncludeFlags()) {
            includeFlags.add(Flags.all);
        }
        String project = validate(opts);
        if (!apiv19) {
            if (opts.isIncludeFlags() && includeFlags.size() > 1) {
                throw new InputError("Cannot use --include: " + includeFlags + " with API < 19");
            }
            if (opts.isIncludeFlags() && !includeFlags.contains(Flags.all)) {
                throw new InputError("Cannot use --include: " + includeFlags + " with API < 19");
            }
            getRdOutput().info(String.format("Export Archive for project: %s", project));
            if (opts.isExecutionIds()) {
                getRdOutput().info(String.format("Contents: only execution IDs: %s", opts.getExecutionIds()));
            } else {
                getRdOutput().info("Contents: all");
            }
            getRdOutput().info("Begin synchronous request...");
            //sync
            receiveArchiveFile(
                    getRdOutput(),
                    apiCall(api -> api.exportProject(project, opts.getExecutionIds())),
                    opts.getFile()
            );
            return 0;
        }
        getRdOutput().info(String.format("Export Archive for project: %s", project));
        if (opts.isExecutionIds()) {
            getRdOutput().info(String.format("Contents: only execution IDs: %s", opts.getExecutionIds()));
        } else {
            getRdOutput().info(String.format("Contents: %s", opts.getIncludeFlags()));
        }
        getRdOutput().info("Begin asynchronous request...");
        ProjectExportStatus status;
        if (opts.isExecutionIds()) {
            status = apiCall(api -> api.exportProjectAsync(
                    project,
                    opts.getExecutionIds()
            ));
        } else {

            status = apiCall(api -> api.exportProjectAsync(
                    project,
                    includeFlags.contains(Flags.all),
                    includeFlags.contains(Flags.jobs),
                    includeFlags.contains(Flags.executions),
                    includeFlags.contains(Flags.configs),
                    includeFlags.contains(Flags.readmes),
                    includeFlags.contains(Flags.acls),
                    includeFlags.contains(Flags.scm)
            ));
        }

        return loopStatus(getRdTool().getClient(), status, project, opts.getFile(), getRdOutput(), () -> {
            try {
                Thread.sleep(2000);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }) ? 0 : 2;
    }

    public static boolean loopStatus(
            final ServiceClient<RundeckApi> client,
            final ProjectExportStatus status,
            String project,
            File outputfile,
            CommandOutput out,
            BooleanSupplier waitFunc
    ) throws IOException
    {
        boolean done = false;
        int perc = status.getPercentage();
        while (!done) {
            ProjectExportStatus status1 = client.apiCall(api -> api.exportProjectStatus(project, status.getToken()));
            if (status1.getPercentage() > perc) {
                out.output(".");
                perc = status1.getPercentage();
            }
            done = status1.getReady();
            if (!done && !waitFunc.getAsBoolean()) {
                    break;
            }
        }
        if (done) {
            receiveArchiveFile(out,
                               client.apiCall(
                                       api -> api.exportProjectDownload(project, status.getToken())
                               ), outputfile
            );
        }
        return done;
    }


    private static void receiveArchiveFile(
            final CommandOutput output, final ResponseBody responseBody, final File file
    )
            throws IOException
    {
        if (!ServiceClient.hasAnyMediaType(responseBody.contentType(), Client.MEDIA_TYPE_ZIP)) {
            throw new IllegalStateException("Unexpected response format: " + responseBody.contentType());
        }
        InputStream inputStream = responseBody.byteStream();
        try (FileOutputStream out = new FileOutputStream(file)) {
            long total = Util.copyStream(inputStream, out);
            output.info(String.format(
                    "Wrote %d bytes of %s to file %s%n",
                    total,
                    responseBody.contentType(),
                    file
            ));
        }
    }
}
