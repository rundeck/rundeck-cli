package org.rundeck.client.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.*;
import org.rundeck.client.util.Json;
import org.rundeck.client.util.Xml;
import retrofit2.Call;
import retrofit2.http.*;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by greg on 3/28/16.
 */
public interface RundeckApi {

    @Headers("Accept: application/json")
    @GET("project/{project}/jobs")
    Call<List<JobItem>> listJobs(
            @Path("project") String project,
            @Query("jobFilter") String jobName,
            @Query("groupPath") String groupPath,
            @Query("jobExactFilter") String jobNameExact,
            @Query("groupPathExact") String groupPathExact
    );

    @Headers("Accept: application/json")
    @GET("project/{project}/jobs")
    Call<List<JobItem>> listJobs(
            @Path("project") String project,
            @Query("idlist") String idlist
    );

    /**
     * new api
     * @param jobid
     * @return
     */
    @Headers("Accept: application/json")
    @GET("job/{jobid}/info")
    Call<ScheduledJobItem> getJobInfo(
            @Path("jobid") String jobid
    );


    @GET("project/{project}/jobs/export")
    Call<ResponseBody> exportJobs(
            @Path("project") String project,
            @Query("jobFilter") String jobName,
            @Query("groupPath") String groupPath,
            @Query("jobExactFilter") String jobNameExact,
            @Query("groupPathExact") String groupPathExact,
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

    /**
     *
     * @see <a href="http://rundeck.org/docs/api/index.html#listing-resources">api</a>
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/resources")
    Call<Map<String, ProjectNode>> listNodes(@Path("project") String project, @Query("filter") String filter);

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

    /**
     * Query executions with all query parameters available
     * @see <a href="http://rundeck.org/docs/api/index.html#execution-query">API</a>
     * @param project
     * @param options
     * @param jobIdListFilter
     * @param xjobIdListFilter
     * @param jobListFilter
     * @param excludeJobListFilters
     * @return
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/executions")
    Call<ExecutionList> listExecutions(
            @Path("project") String project,
            @QueryMap Map<String, String> options,
            @Query("jobIdListFilter") List<String> jobIdListFilter,
            @Query("excludeJobIdListFilter") List<String> xjobIdListFilter,
            @Query("jobListFilter") List<String> jobListFilter,
            @Query("excludeJobListFilter") List<String> excludeJobListFilters
    );

    /**
     * Bulk delete
     *
     * @param delete
     *
     * @return
     */
    @Json
    @Headers("Accept: application/json")
    @POST("executions/delete")
    Call<BulkExecutionDeleteResponse> deleteExecutions(
            @Body BulkExecutionDelete delete
    );

    @Headers("Accept: application/json")
    @GET("execution/{id}/abort")
    Call<AbortResult> abortExecution(@Path("id") String id);

    @Headers("Accept: application/json")
    @GET("execution/{id}")
    Call<Execution> getExecution(@Path("id") String id);


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

    @Headers("Accept: application/json")
    @POST("job/{id}/executions")
    Call<Execution> runJob(
            @Path("id") String id,
            @Query("argString") String argString,
            @Query("loglevel") String loglevel,
            @Query("filter") String filter,
            @Query("asUser") String user
    );

    @Headers("Accept: application/json")
    @POST("job/{id}/executions")
    Call<Execution> runJob(
            @Path("id") String id,
            @Body JobRun jobRun

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

    //system ACLs
    @Headers("Accept: application/json")
    @GET("system/acl/")
    Call<ACLPolicyItem> listSystemAcls(

    );

    @Headers("Accept: application/json")
    @GET("system/acl/{name}")
    Call<ACLPolicy> getSystemAclPolicy(
            @Path("name") String name
    );

    @Headers("Accept: application/json")
    @PUT("system/acl/{name}")
    Call<ACLPolicy> updateSystemAclPolicy(
            @Path("name") String name,
            @Body RequestBody body
    );

    @Headers("Accept: application/json")
    @POST("system/acl/{name}")
    Call<ACLPolicy> createSystemAclPolicy(
            @Path("name") String name,
            @Body RequestBody body
    );

    @Headers("Accept: application/json")
    @DELETE("system/acl/{name}")
    Call<Void> deleteSystemAclPolicy(
            @Path("name") String name
    );

    @Headers("Accept: application/json")
    @GET("system/info")
    Call<SystemInfo> systemInfo();

    //scheduler

    /**
     * List scheduler owned jobs for the target server
     *
     * @return list of jobs
     */
    @Headers("Accept: application/json")
    @GET("scheduler/jobs")
    Call<List<ScheduledJobItem>> listSchedulerJobs();

    /**
     * List scheduler owned jobs for the specified server
     *
     * @param uuid server uuid
     *
     * @return list of jobs
     */
    @Headers("Accept: application/json")
    @GET("scheduler/server/{uuid}/jobs")
    Call<List<ScheduledJobItem>> listSchedulerJobs(@Path("uuid") String uuid);


    /**
     * <a href="http://rundeck.org/docs/api/index.html#get-project-scm-config">
     * Get SCM Config for a project
     * </a>
     *
     * @param project     project name
     * @param integration integration type
     *
     * @return config
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/scm/{integration}/config")
    Call<ScmConfig> getScmConfig(@Path("project") String project, @Path("integration") String integration);

    /**
     * <a href="http://rundeck.org/docs/api/index.html#setup-scm-plugin-for-a-project">Setup SCM Config for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     * @param type        plugin type
     * @param body        request body
     *
     * @return result
     */
    @Headers("Accept: application/json")
    @POST("project/{project}/scm/{integration}/plugin/{type}/setup")
    Call<ScmActionResult> setupScmConfig(
            @Path("project") String project,
            @Path("integration") String integration,
            @Path("type") String type,
            @Body RequestBody body
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#list-tokens">List Tokens</a>
     *
     * @return list of tokens
     */
    @Headers("Accept: application/json")
    @GET("tokens}")
    Call<List<ApiToken>> listTokens();

    /**
     * <a href="http://rundeck.org/docs/api/index.html#list-tokens">List Tokens</a>
     *
     * @param user username
     *
     * @return list of tokens for a user
     */
    @Headers("Accept: application/json")
    @GET("tokens/{user}")
    Call<List<ApiToken>> listTokens(
            @Path("user") String user
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#create-a-token">Create a Token</a>
     *
     * @param user username
     *
     * @return created token
     */
    @Headers("Accept: application/json")
    @POST("tokens/{user}")
    Call<ApiToken> createToken(
            @Path("user") String user
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#delete-a-token">Delete a Token</a>
     *
     * @param id token
     */
    @Headers("Accept: application/json")
    @DELETE("token/{id}")
    Call<Void> deleteToken(
            @Path("id") String id
    );


    /**
     * <a href="http://rundeck.org/docs/api/index.html#get-readme-file">Get Readme File</a>
     *
     * @param project project
     * @param file    type of readme file
     *
     * @return readme contents
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/{file}")
    Call<ProjectReadme> getReadme(
            @Path("project") String project,
            @Path("file") ReadmeFile file
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#put-readme-file">Put Readme File</a>
     *
     * @param project project
     * @param file    type of readme file
     *
     * @return readme contents
     */
    @Headers("Accept: application/json")
    @PUT("project/{project}/{file}")
    Call<ProjectReadme> putReadme(
            @Path("project") String project,
            @Path("file") ReadmeFile file,
            @Body RequestBody contents
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#get-readme-file">Get Readme File</a>
     *
     * @param project project
     * @param file    type of readme file
     *
     * @return readme contents
     */
    @Headers("Accept: application/json")
    @DELETE("project/{project}/{file}")
    Call<Void> deleteReadme(
            @Path("project") String project,
            @Path("file") ReadmeFile file
    );

}
