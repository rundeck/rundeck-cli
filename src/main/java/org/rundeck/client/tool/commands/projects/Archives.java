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

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.simplifyops.toolbelt.Command;
import com.simplifyops.toolbelt.CommandOutput;
import com.simplifyops.toolbelt.InputError;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ProjectExportStatus;
import org.rundeck.client.api.model.ProjectImportStatus;
import org.rundeck.client.tool.RdApp;
import org.rundeck.client.tool.commands.AppCommand;
import org.rundeck.client.tool.options.ProjectNameOptions;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * @author greg
 * @since 4/10/17
 */

@Command(description = "Project Archives import and export")
public class Archives extends AppCommand {
    public Archives(final RdApp rdApp) {
        super(rdApp);
    }

    @CommandLineInterface(application = "import") interface ArchiveImportOpts
            extends ArchiveFileOpts,
            ProjectNameOptions
    {
        @Option(shortName = "r", description = "Remove Job UUIDs in imported jobs. Default: preserve job UUIDs.")
        boolean isRemove();

        @Option(shortName = "x",
                description = "Do not include executions in import. Default: do include executions in import.")
        boolean isNoExecutions();

        @Option(shortName = "c",
                longName = "include-config",
                description = "Include project configuration in import, default: false")
        boolean isIncludeConfig();

        @Option(shortName = "a", longName = "include-acl", description = "Include ACLs in import, default: false")
        boolean isIncludeAcl();

        @Option(description = "Return non-zero exit status if any imported item had an error. Default: only job " +
                              "import errors are treated as failures.")
        boolean isStrict();

    }

    @Command(description = "Import a project archive", value = "import")
    public boolean importArchive(ArchiveImportOpts opts, CommandOutput out) throws InputError, IOException {
        File input = opts.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new InputError(String.format("File is not readable or does not exist: %s", input));
        }

        RequestBody body = RequestBody.create(Client.MEDIA_TYPE_ZIP, input);

        String project = projectOrEnv(opts);
        ProjectImportStatus status = apiCall(api -> api.importProjectArchive(
                project,
                opts.isRemove() ? "remove" : "preserve",
                !opts.isNoExecutions(),
                opts.isIncludeConfig(),
                opts.isIncludeAcl(),
                body
        ));
        boolean anyerror = false;
        if (status.getResultSuccess()) {
            out.info("Jobs imported successfully");
        } else {
            anyerror = true;
            if (null != status.errors && status.errors.size() > 0) {
                out.error("Some imported Jobs failed:");
                out.error(status.errors);
            }
        }
        if (null != status.executionErrors && status.executionErrors.size() > 0) {
            anyerror = true;
            out.error("Some imported executions failed:");
            out.error(status.executionErrors);
        }
        if (null != status.aclErrors && status.aclErrors.size() > 0) {
            anyerror = true;
            out.error("Some imported ACLs failed:");
            out.error(status.aclErrors);
        }

        return opts.isStrict() ? !anyerror : status.getResultSuccess();
    }

    interface ArchiveFileOpts {

        @Option(shortName = "f", description = "Output file path")
        File getFile();
    }

    @CommandLineInterface(application = "export") interface ArchiveExportOpts
            extends ArchiveFileOpts,
            ProjectNameOptions
    {

        @Option(
                longName = "execids",
                shortName = "e",
                description = "List of execution IDs. Exports only those ids.")
        List<String> getExecutionIds();

        boolean isExecutionIds();

        @Option(
                longName = "include",
                shortName = "i",
                description =
                        "List of archive contents to include. [all,jobs,executions,configs,readmes,acls]. Default: " +
                        "all. (API v19 required for other " +
                        "options).")
        Set<Flags> getIncludeFlags();

        boolean isIncludeFlags();

    }

    enum Flags {
        all,
        jobs,
        executions,
        configs,
        readmes,
        acls
    }

    @Command(description = "Export a project archive")
    public boolean export(ArchiveExportOpts opts, CommandOutput output) throws IOException, InputError {
        if (opts.isIncludeFlags() && opts.isExecutionIds()) {
            throw new InputError("Cannot use --execids/-e with --include/-i");
        }
        boolean apiv19 = getClient().getApiVersion() >= 19;

        Set<Flags> includeFlags = opts.isIncludeFlags() ? opts.getIncludeFlags() : new HashSet<>();
        if (!opts.isIncludeFlags()) {
            includeFlags.add(Flags.all);
        }
        String project = projectOrEnv(opts);
        if (!apiv19) {
            if (opts.isIncludeFlags() && includeFlags.size() > 1) {
                throw new InputError("Cannot use --include: " + includeFlags + " with API < 19");
            }
            if (opts.isIncludeFlags() && !includeFlags.contains(Flags.all)) {
                throw new InputError("Cannot use --include: " + includeFlags + " with API < 19");
            }
            output.info(String.format("Export Archive for project: %s", project));
            if (opts.isExecutionIds()) {
                output.info(String.format("Contents: only execution IDs: %s", opts.getExecutionIds()));
            } else {
                output.info("Contents: all");
            }
            output.info("Begin synchronous request...");
            //sync
            receiveArchiveFile(
                    output,
                    apiCall(api -> api.exportProject(project, opts.getExecutionIds())),
                    opts.getFile()
            );
            return true;
        }
        output.info(String.format("Export Archive for project: %s", project));
        if (opts.isExecutionIds()) {
            output.info(String.format("Contents: only execution IDs: %s", opts.getExecutionIds()));
        } else {
            output.info(String.format("Contents: %s", opts.getIncludeFlags()));
        }
        output.info("Begin asynchronous request...");
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
                    includeFlags.contains(Flags.acls)
            ));
        }

        return loopStatus(getClient(), status, project, opts.getFile(), output, () -> {
            try {
                Thread.sleep(2000);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        });
    }

    public static boolean loopStatus(
            final Client<RundeckApi> client,
            final ProjectExportStatus status,
            String project,
            File outputfile,
            CommandOutput out,
            BooleanSupplier waitFunc
    ) throws IOException, InputError
    {
        boolean done = false;
        int perc = status.getPercentage();
        while (!done) {
            ProjectExportStatus status1 = apiCall(client, api -> api.exportProjectStatus(project, status.getToken()));
            if (status1.getPercentage() > perc) {
                out.output(".");
                perc = status1.getPercentage();
            }
            done = status1.getReady();
            if (!done) {
                if (!waitFunc.getAsBoolean()) {
                    break;
                }
            }
        }
        if (done) {
            receiveArchiveFile(out,
                               apiCall(
                                       client,
                                       api -> api.exportProjectDownload(project, status.getToken())
                               ), outputfile
            );
        }
        return done;
    }


    private static void receiveArchiveFile(
            final CommandOutput output, final ResponseBody responseBody, final File file
    )
            throws InputError, IOException
    {
        if (!Client.hasAnyMediaType(responseBody, Client.MEDIA_TYPE_ZIP)) {
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
