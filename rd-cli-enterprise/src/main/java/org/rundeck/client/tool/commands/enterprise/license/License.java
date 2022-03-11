package org.rundeck.client.tool.commands.enterprise.license;

import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import org.rundeck.client.tool.commands.enterprise.BaseExtension;
import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseResponse;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseStoreResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.util.Format;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.SubCommand;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

@Command()
@SubCommand(path = {"license"}, descriptions = {"Manage Rundeck Enterprise License"})
public class License
        extends BaseExtension
{

    public static final MediaType LICENSE_KEY_MEDIA_TYPE = MediaType.parse("application/x-rundeck-license");

    @CommandLineInterface
    interface StatusOpts {
        @Option(shortName = "v", longName = "verbose", description = "Show verbose output")
        boolean isVerbose();

        @Option(shortName = "s",
                longName = "status",
                description = "Succeed if license status is active, fail if not active.")
        boolean isStatus();

        @Option(shortName = "r",
                longName = "remaining",
                description = "Fail if fewer than the specified days remain in license.")
        int getRemaining();

        boolean isRemaining();

        @Option(shortName = "%",
                longName = "outformat",
                description = "Output format specifier for license data. You can use \"%key\" where key is one of:"
                              + " applicationVersion, gracePeriod, reason, validSince, contactEmail, shouldWarn, "
                              + "active, edition, type, perpetual, invalidCode, remaining, baseUrl, editions, "
                              + "application, serverUUIDs, validUntil, warning, company, state, issueDate, licenseId,"
                              + " graceUntil. E.g. \"%state, %remaining\"")
        String getOutputFormat();

        boolean isOutputFormat();
    }

    @Command(description = "license status")
    public boolean status(StatusOpts opts) throws InputError, IOException {
        RdTool.apiVersionCheck("license status", 41, getClient().getApiVersion());
        LicenseResponse response = getClient().apiCall(EnterpriseApi::verifyLicense);

        if (opts.isOutputFormat()) {
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

        if (opts.isRemaining() && response.getRemaining() < opts.getRemaining()) {
            if (!opts.isOutputFormat()) {
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


    @CommandLineInterface
    interface StoreOpts {
        @Option(shortName = "v", longName = "verbose", description = "Show verbose output")
        boolean isVerbose();

        @Option(longName = "agree", description = "Agree to the license")
        boolean isAgree();

        @Option(shortName = "f", longName = "file", description = "Rundeck Enterprise License Key File")
        File getFile();

    }

    @Command(description = "Store license")
    public boolean store(StoreOpts opts) throws InputError, IOException {
        RdTool.apiVersionCheck("Store License", 41, getClient().getApiVersion());
        RequestBody body = RequestBody.create(opts.getFile(), LICENSE_KEY_MEDIA_TYPE);
        LicenseStoreResponse response = getClient().apiCall(api -> api.storeLicense(body, opts.isAgree()));

        getOutput().output(response);

        return true;
    }
}
