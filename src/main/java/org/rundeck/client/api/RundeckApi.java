package org.rundeck.client.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.*;
import org.rundeck.client.util.Xml;
import retrofit2.Call;
import retrofit2.http.*;

import java.net.URL;
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
    Call<ExecutionList> runningExecutions(
            @Path("project") String project,
            @Query("offset") int offset,
            @Query("max") int max
    );

    @Headers("Accept: application/json")
    @GET("project/{project}/executions")
    Call<ExecutionList> listExecutions(
            @Path("project") String project,
            @Query("offset") int offset,
            @Query("max") int max,
            @Query("olderFilter") String olderThan,
            @Query("recentFilter") String newerThan
    );

    @Headers("Accept: application/json")
    @GET("execution/{id}/abort")
    Call<AbortResult> abortExecution(@Path("id") String id);


    @Headers("Accept: application/json")
    @DELETE("execution/{id}")
    Call<Void> deleteExecution(@Path("id") String id);


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


    @Headers("Accept: application/json")
    @POST("project/{project}/run/command")
    Call<Execution> runCommand(
            @Path("project") String project,
            @Query("exec") String command,
            @Query("filter") String filter
    );

    @Headers("Accept: application/json")
    @POST("project/{project}/run/command")
    Call<AdhocResponse> runCommand(
            @Path("project") String project,
            @Query("exec") String command,
            @Query("nodeThreadcount") int threadcount,
            @Query("nodeKeepgoing") boolean keepgoing,
            @Query("filter") String filter
    );


    @Multipart
    @Headers("Accept: application/json")
    @POST("project/{project}/run/script")
    Call<AdhocResponse> runScript(
            @Path("project") String project,
            @Part MultipartBody.Part scriptFile,
            @Query("argString") String argString,
            @Query("scriptInterpreter") String scriptInterpreter,
            @Query("interpreterArgsQuoted") boolean interpreterArgsQuoted,
            @Query("fileExtension") String fileExtension,
            @Query("filter") String filter
    );

    @Multipart
    @Headers("Accept: application/json")
    @POST("project/{project}/run/script")
    Call<AdhocResponse> runScript(
            @Path("project") String project,
            @Part MultipartBody.Part scriptFile,
            @Query("nodeThreadcount") int threadcount,
            @Query("nodeKeepgoing") boolean keepgoing,
            @Query("argString") String argString,
            @Query("scriptInterpreter") String scriptInterpreter,
            @Query("interpreterArgsQuoted") boolean interpreterArgsQuoted,
            @Query("fileExtension") String fileExtension,
            @Query("filter") String filter
    );


    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("project/{project}/run/url")
    Call<Execution> runUrl(
            @Path("project") String project,
            @Field("scriptURL") URL url,
            @Field("argString") String argString,
            @Field("scriptInterpreter") String scriptInterpreter,
            @Field("interpreterArgsQuoted") boolean interpreterArgsQuoted,
            @Field("fileExtension") String fileExtension,
            @Field("filter") String filter
    );

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("project/{project}/run/url")
    Call<AdhocResponse> runUrl(
            @Path("project") String project,
            @Field("scriptURL") URL url,
            @Field("nodeThreadcount") int threadcount,
            @Field("nodeKeepgoing") boolean keepgoing,
            @Field("argString") String argString,
            @Field("scriptInterpreter") String scriptInterpreter,
            @Field("interpreterArgsQuoted") boolean interpreterArgsQuoted,
            @Field("fileExtension") String fileExtension,
            @Field("filter") String filter
    );

    @Headers("Accept: application/json")
    @POST("job/{id}/run")
    Call<Execution> runJob(
            @Path("id") String id,
            @Query("argString") String argString,
            @Query("loglevel") String loglevel,
            @Query("filter") String filter
    );

    //key storage

    @Headers("Accept: application/json")
    @GET("storage/keys/{path}")
    Call<KeyStorageItem> listKeyStorage(
            @Path(value = "path", encoded = true) String path
    );

    @Headers("Accept: application/pgp-keys")
    @GET("storage/keys/{path}")
    Call<ResponseBody> getPublicKey(
            @Path(value = "path", encoded = true) String path
    );

    @Headers("Accept: application/json")
    @DELETE("storage/keys/{path}")
    Call<Void> deleteKeyStorage(
            @Path(value = "path", encoded = true) String path
    );

    @Headers("Accept: application/json")
    @POST("storage/keys/{path}")
    Call<KeyStorageItem> createKeyStorage(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody body
    );

    @Headers("Accept: application/json")
    @PUT("storage/keys/{path}")
    Call<KeyStorageItem> updateKeyStorage(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody body
    );

    //project ACLs
    @Headers("Accept: application/json")
    @GET("project/{project}/acl/")
    Call<ACLPolicyItem> listAcls(
            @Path("project") String project
    );

    @Headers("Accept: application/json")
    @GET("project/{project}/acl/{name}")
    Call<ACLPolicy> getAclPolicy(
            @Path("project") String project,
            @Path("name") String name
    );

    @Headers("Accept: application/json")
    @PUT("project/{project}/acl/{name}")
    Call<ACLPolicy> updateAclPolicy(
            @Path("project") String project,
            @Path("name") String name,
            @Body RequestBody body
    );

    @Headers("Accept: application/json")
    @POST("project/{project}/acl/{name}")
    Call<ACLPolicy> createAclPolicy(
            @Path("project") String project,
            @Path("name") String name,
            @Body RequestBody body
    );

    @Headers("Accept: application/json")
    @DELETE("project/{project}/acl/{name}")
    Call<Void> deleteAclPolicy(
            @Path("project") String project,
            @Path("name") String name
    );
}
