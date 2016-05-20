package org.rundeck.client.tool;

import com.lexicalscope.jewel.cli.CliFactory;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.tool.options.JobListOptions;
import retrofit2.Call;
import retrofit2.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by greg on 3/28/16.
 */
public class Jobs {
    public static void main(String[] args) throws IOException {
        String baseUrl = App.requireEnv("RUNDECK_URL", "Please specify the Rundeck URL");
        String token = App.requireEnv("RUNDECK_TOKEN", "Please specify the Rundeck authentication Token");
        RundeckApi client = Rundeck.client(baseUrl, token, true);
        if ("list".equals(args[0])) {
            list(App.tail(args), client);
        }

    }

    private static void list(final String[] args, final RundeckApi client) throws IOException {
        JobListOptions jobListOptions = CliFactory.parseArguments(JobListOptions.class, args);


        if (jobListOptions.isFile()) {
            //write response to file instead of parsing it
            Call<ResponseBody> responseCall;
            if (jobListOptions.isIdlist()) {
                responseCall = client.readJobs(
                        jobListOptions.getProject(),
                        jobListOptions.getIdlist(),
                        jobListOptions.getFormat()
                );
            } else {
                responseCall = client.readJobs(
                        jobListOptions.getProject(),
                        jobListOptions.getJob(),
                        jobListOptions.getGroup(),
                        jobListOptions.getFormat()
                );
            }
            Response<ResponseBody> execute = responseCall.execute();
            if (!execute.isSuccess()) {
                return;
            }
            ResponseBody body = execute.body();
            if ((!"yaml".equals(jobListOptions.getFormat()) ||
                 !body.contentType().equals(MediaType.parse("text/yaml"))) &&
                !body.contentType().equals(MediaType.parse("text/xml"))) {

                throw new IllegalStateException("Unexpected response format: " + body.contentType());
            }
            InputStream inputStream = body.byteStream();
            long total = 0;
            try (FileOutputStream out = new FileOutputStream(jobListOptions.getFile())) {
                byte[] buff = new byte[10240];
                int count = inputStream.read(buff);
                while (count > 0) {
                    out.write(buff, 0, count);
                    total += count;
                    count = inputStream.read(buff);
                }
            }
            System.out.printf("Wrote %d bytes of %s to file %s%n", total, body.contentType(), jobListOptions.getFile());
        } else {
            Call<List<JobItem>> listCall;
            if (jobListOptions.isIdlist()) {
                listCall = client.listJobs(jobListOptions.getProject(), jobListOptions.getIdlist());
            } else {
                listCall = client.listJobs(
                        jobListOptions.getProject(),
                        jobListOptions.getJob(),
                        jobListOptions.getGroup()
                );
            }
            List<JobItem> body = listCall.execute().body();
            for (JobItem jobItem : body) {
                System.out.println("* " + jobItem.toBasicString());
            }
        }
    }
}
