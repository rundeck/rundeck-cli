package org.rundeck.client.tool.commands.enterprise.license;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import org.rundeck.client.tool.commands.enterprise.BaseExtension;
import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseResponse;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseStoreResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.util.Format;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;


@CommandLine.Command(name = "license", description = "Manage Rundeck Enterprise License")
public class License
        extends BaseExtension {

    public static final MediaType LICENSE_KEY_MEDIA_TYPE = MediaType.parse("application/x-rundeck-license");

    @Getter
    @Setter
    static class StatusOpts {
        @CommandLine.Option(names = {"-v", "--verbose"}, description = "Show verbose output")
        private boolean verbose;

        @CommandLine.Option(names = {"-s",
                "--status"},
                description = "Succeed if license status is active, fail if not active.")
        boolean status;

        @CommandLine.Option(names = {"-r", "--remaining"},
                description = "Fail if fewer than the specified days remain in license.")
        Integer remaining;


        @CommandLine.Option(names = {"-%", "--outformat"},
                description = "Output format specifier for license data. You can use \"%%key\" where key is one of:"
                        + " applicationVersion, gracePeriod, reason, validSince, contactEmail, shouldWarn, "
                        + "active, edition, type, perpetual, invalidCode, remaining, baseUrl, editions, "
                        + "application, serverUUIDs, validUntil, warning, company, state, issueDate, licenseId,"
                        + " graceUntil. E.g. \"%%state, %%remaining\"")
        String outputFormat;
    }

    @CommandLine.Command(description = "license status")
    public boolean status(@CommandLine.Mixin StatusOpts opts) throws InputError, IOException {
        RdTool.apiVersionCheck("license status", 41, getClient().getApiVersion());
        LicenseResponse response = getClient().apiCall(EnterpriseApi::verifyLicense);

        if (opts.getOutputFormat() != null) {
            final Function<LicenseResponse, ?>
                    outformat =
                    Format.formatter(opts.getOutputFormat(), LicenseResponse::asMap, "%", "");
            getOutput().output(outformat.apply(response));
        } else {
            String status = String.format("Status: %s", response.getState());
            if (response.isActive()) {
                getOutput().info(status);
            } else {
                getOutput().warning(status);
            }
            if (response.getReason() != null) {
                getOutput().info(response.getReason());
            }
            if (response.isShouldWarn()) {
                getOutput().warning(response.getWarning());
            }
            if (opts.isVerbose()) {
                getOutput().output(response);
            }
        }

        if (opts.remaining != null && response.getRemaining() < opts.getRemaining()) {
            if (opts.outputFormat == null) {
                getOutput().error(String.format(
                        "Days remaining: %d < %d",
                        response.getRemaining(),
                        opts.getRemaining()
                ));
            }
            return false;
        }
        return !opts.isStatus() || response.isActive();
    }


    @CommandLine.Command(description = "Store license")
    public void store(
            @CommandLine.Option(names = {"--agree"}, description = "Agree to the license")
            boolean agree,
            @CommandLine.Option(names = {"-f", "--file"}, description = "Rundeck Enterprise License Key File", required = true)
            File file
    ) throws InputError, IOException {
        RdTool.apiVersionCheck("Store License", 41, getClient().getApiVersion());
        RequestBody body = RequestBody.create(file, LICENSE_KEY_MEDIA_TYPE);
        LicenseStoreResponse response = getClient().apiCall(api -> api.storeLicense(body, agree));

        getOutput().output(response);

    }
}
