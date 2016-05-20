package org.rundeck.client.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.*;
import org.rundeck.client.util.Xml;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

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

    @Xml
    @Headers("Accept: application/xml")
    @POST("project/{project}/jobs/import")
    Call<ImportResult> loadJobs(@Path("project") String project, @Body RequestBody body);

    @Xml
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
    @DELETE("jobs/delete")
    Call<DeleteJobsResult> deleteJobs(
            @Query("ids") List<String> ids
    );

    @Headers("Accept: application/json")
    @GET("projects")
    Call<List<ProjectItem>> listProjects();

    @Headers("Accept: application/json")
    @POST("projects")
    Call<ProjectItem> createProject(@Body ProjectItem properties);

    @DELETE("project/{project}")
    Call<Void> deleteProject(@Path("project") String project);


    @Headers("Accept: application/json")
    @GET("project/{project}/executions/running")
    Call<ExecutionList> listExecutions(
            @Path("project") String project,
            @Query("offset") int offset,
            @Query("max") int max
    );

    @Headers("Accept: application/json")
    @GET("execution/{id}/abort")
    Call<AbortResult> abortExecution(@Path("id") String id);


    @Headers("Accept: application/json")
    @GET("execution/{id}/output")
    Call<ExecOutput> getOutput(
            @Path("id") String id,
            @Query("lastlines") Long lastlines
    );

    @Headers("Accept: application/json")
    @GET("execution/{id}/output")
    Call<ExecOutput> getOutput(
            @Path("id") String id,
            @Query("offset") Long offset,
            @Query("lastmod") Long lastmod,
            @Query("maxlines") Long maxlines
    );


}
