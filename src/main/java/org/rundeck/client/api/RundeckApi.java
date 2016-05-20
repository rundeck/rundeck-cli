package org.rundeck.client.api;

import okhttp3.ResponseBody;
import org.rundeck.client.api.model.JobItem;
import org.rundeck.client.api.model.ProjectItem;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.List;
import java.util.Properties;

/**
 * Created by greg on 3/28/16.
 */
public interface RundeckApi {

    @GET("project/{project}/jobs")
    Call<List<JobItem>> listJobs(
            @Path("project") String project,
            @Query("jobFilter") String jobName,
            @Query("groupPath") String groupPath
    );

    @GET("project/{project}/jobs")
    Call<List<JobItem>> listJobs(
            @Path("project") String project,
            @Query("idlist") String idlist
    );


    @GET("project/{project}/jobs")
    Call<ResponseBody> readJobs(
            @Path("project") String project,
            @Query("jobFilter") String jobName,
            @Query("groupPath") String groupPath,
            @Query("format") String format
    );

    @GET("project/{project}/jobs")
    Call<ResponseBody> readJobs(
            @Path("project") String project,
            @Query("idlist") String idlist,
            @Query("format") String format
    );

    @GET("projects")
    Call<List<ProjectItem>> listProjects();

    @POST("projects")
    Call<ProjectItem> createProject(@Body ProjectItem properties);

    @DELETE("project/{project}")
    Call<Void> deleteProject(@Path("project") String project);
}
