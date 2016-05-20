package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.ImportResult;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.JobLoadItem;
import org.rundeck.client.tool.options.JobListOptions;
import org.rundeck.client.tool.options.JobLoadOptions;
import retrofit2.Call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by greg on 3/28/16.
 */
public class Jobs {

    public static final String APPLICATION_XML = "application/xml";
    public static final MediaType MEDIA_TYPE_XML = MediaType.parse(APPLICATION_XML);
    public static final String APPLICATION_YAML = "application/yaml";
    public static final MediaType MEDIA_TYPE_YAML = MediaType.parse(APPLICATION_YAML);
    public static final String UUID_REMOVE = "remove";
    public static final String UUID_PRESERVE = "preserve";
    public static final MediaType MEDIA_TYPE_TEXT_YAML = MediaType.parse("text/yaml");
    public static final MediaType MEDIA_TYPE_TEXT_XML = MediaType.parse("text/xml");

    public static void main(String[] args) throws IOException {
        String baseUrl = App.requireEnv("RUNDECK_URL", "Please specify the Rundeck URL");
        String token = App.requireEnv("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
        RundeckApi client = Rundeck.client(baseUrl, token, true);
        String[] actions = new String[]{"list", "load"};
        if ("list".equals(args[0])) {
            list(App.tail(args), client);
        } else if ("load".equals(args[0])) {
            load(App.tail(args), client);
        } else {

            throw new IllegalArgumentException(String.format("Unrecognized action: %s, expected one of %s", args[0],
                                                             Arrays.asList(actions)
            ));
        }

    }

    private static boolean load(final String[] args, final RundeckApi client) throws IOException {
        JobLoadOptions options = CliFactory.parseArguments(JobLoadOptions.class, args);
        if (!options.isFile()) {
            throw new IllegalArgumentException("-f is required");
        }
        File input = options.getFile();
        if (!input.canRead() || !input.isFile()) {
            throw new IllegalArgumentException(String.format("File is not readable or does not exist: %s", input));
        }

        RequestBody requestBody = RequestBody.create(
                "xml".equals(options.getFormat()) ? MEDIA_TYPE_XML : MEDIA_TYPE_YAML,
                input
        );

        Call<ImportResult> importResultCall = client.loadJobs(
                options.getProject(),
                requestBody,
                options.getFormat(),
                options.getDuplicate(),
                options.isRemoveUuids() ? UUID_REMOVE : UUID_PRESERVE
        );
        ImportResult importResult = App.checkError(importResultCall);

        List<JobLoadItem> failed = importResult.getFailed();

        printLoadResult(importResult.getSucceeded(), "Succeeded");
        printLoadResult(importResult.getSkipped(), "Skipped");
        printLoadResult(failed, "Failed");

        return failed == null || failed.size() == 0;
    }

    private static void printLoadResult(final List<JobLoadItem> list, final String title) {
        System.out.printf("%d Jobs " + title + ":%n", list != null ? list.size() : 0);
        if (null != list && list.size() > 0) {
            for (JobLoadItem jobLoadItem : list) {
                System.out.printf("* %s%n", jobLoadItem.toBasicString());
            }
        }
    }

    private static void list(final String[] args, final RundeckApi client) throws IOException {
        JobListOptions options = CliFactory.parseArguments(JobListOptions.class, args);


        if (options.isFile()) {
            //write response to file instead of parsing it
            Call<ResponseBody> responseCall;
            if (options.isIdlist()) {
                responseCall = client.exportJobs(
                        options.getProject(),
                        options.getIdlist(),
                        options.getFormat()
                );
            } else {
                responseCall = client.exportJobs(
                        options.getProject(),
                        options.getJob(),
                        options.getGroup(),
                        options.getFormat()
                );
            }
            ResponseBody body = App.checkError(responseCall);
            if ((!"yaml".equals(options.getFormat()) ||
                 !hasAnyMediaType(body, MEDIA_TYPE_YAML, MEDIA_TYPE_TEXT_YAML)) &&
                !hasAnyMediaType(body, MEDIA_TYPE_XML, MEDIA_TYPE_TEXT_XML)) {

                throw new IllegalStateException("Unexpected response format: " + body.contentType());
            }
            InputStream inputStream = body.byteStream();
            long total = 0;
            try (FileOutputStream out = new FileOutputStream(options.getFile())) {
                byte[] buff = new byte[10240];
                int count = inputStream.read(buff);
                while (count > 0) {
                    out.write(buff, 0, count);
                    total += count;
                    count = inputStream.read(buff);
                }
            }
            System.out.printf("Wrote %d bytes of %s to file %s%n", total, body.contentType(), options.getFile());
        } else {
            Call<List<JobItem>> listCall;
            if (options.isIdlist()) {
                listCall = client.listJobs(options.getProject(), options.getIdlist());
            } else {
                listCall = client.listJobs(
                        options.getProject(),
                        options.getJob(),
                        options.getGroup()
                );
            }
            List<JobItem> body = App.checkError(listCall);
            System.out.printf("%d Jobs in project %s%n", body.size(), options.getProject());
            for (JobItem jobItem : body) {
                System.out.println("* " + jobItem.toBasicString());
            }
        }
    }

    private static boolean hasAnyMediaType(final ResponseBody body, final MediaType... parse) {
        MediaType mediaType1 = body.contentType();
        for (MediaType mediaType : parse) {
            if (mediaType1.type().equals(mediaType.type()) && mediaType1.subtype().equals(mediaType.subtype())) {
                return true;
            }
        }
        return false;
    }
}
