package org.rundeck.client.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.ImportResult;
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

    @Headers("Accept: application/json")
    @GET("project/{project}/jobs")
    Call<List<JobItem>> listJobs(
            @Path("project") String project,
            @Query("jobFilter") String jobName,
            @Query("groupPath") String groupPath
    );

    @Headers("Accept: application/json")
    @GET("project/{project}/jobs")
    Call<List<JobItem>> listJobs(
            @Path("project") String project,
            @Query("idlist") String idlist
    );


    @GET("project/{project}/jobs/export")
    Call<ResponseBody> exportJobs(
            @Path("project") String project,
            @Query("jobFilter") String jobName,
            @Query("groupPath") String groupPath,
            @Query("format") String format
    );

    @GET("project/{project}/jobs/export")
    Call<ResponseBody> exportJobs(
            @Path("project") String project,
            @Query("idlist") String idlist,
            @Query("format") String format
    );

    @Headers("Accept: application/xml")
    @POST("project/{project}/jobs/import")
    Call<ImportResult> loadJobs(@Path("project") String project, @Body RequestBody body);

    @Headers("Accept: application/xml")
    @POST("project/{project}/jobs/import")
    Call<ImportResult> loadJobs(
            @Path("project") String project,
            @Body RequestBody body,
            @Query("format") String format,
            @Query("dupeOption") String duplicate,
            @Query("uuidOption") String uuids
    );

    @Headers("Accept: application/json")
    @GET("projects")
    Call<List<ProjectItem>> listProjects();

    @Headers("Accept: application/json")
    @POST("projects")
    Call<ProjectItem> createProject(@Body ProjectItem properties);

    @DELETE("project/{project}")
    Call<Void> deleteProject(@Path("project") String project);
}
