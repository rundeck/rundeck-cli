package org.rundeck.client.api;

import org.rundeck.client.api.model.JobItem;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

/**
 * Created by greg on 3/28/16.
 */
public interface RundeckApi {

    @GET("project/{project}/jobs")
    Call<List<JobItem>> listJobs(@Path("project") String project);
}
