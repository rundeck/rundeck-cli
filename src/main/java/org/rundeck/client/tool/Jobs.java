package org.rundeck.client.tool;

import org.rundeck.client.Rundeck;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.api.model.JobItem;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 3/28/16.
 */
public class Jobs {
    public static void main(String[] args) throws IOException {
        String project = args[0];
        String token = System.getenv("RUNDECK_TOKEN");
        String baseUrl = System.getenv("RUNDECK_URL");
        RundeckApi client = Rundeck.client(baseUrl, token);
        Call<List<JobItem>> listCall = client.listJobs(project);
        List<JobItem> body = listCall.execute().body();
        for (JobItem jobItem : body) {
            System.out.println("* " + jobItem.toBasicString());
        }
    }
}
