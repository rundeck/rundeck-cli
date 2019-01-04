/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.*;
import org.rundeck.client.api.model.pro.*;
import org.rundeck.client.api.model.metrics.EndpointListResult;
import org.rundeck.client.api.model.metrics.HealthCheckStatus;
import org.rundeck.client.api.model.metrics.MetricsData;
import org.rundeck.client.api.model.repository.ArtifactActionMessage;
import org.rundeck.client.api.model.repository.RepositoryArtifacts;
import org.rundeck.client.api.model.scheduler.ScheduledJobItem;
import org.rundeck.client.api.model.scheduler.SchedulerTakeover;
import org.rundeck.client.api.model.scheduler.SchedulerTakeoverResult;
import org.rundeck.client.util.Json;
import org.rundeck.client.util.Xml;
import retrofit2.Call;
import retrofit2.http.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Interface for Rundeck API using retrofit annotations
 */
@SuppressWarnings("JavaDoc")
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

    /**
     * Get uploaded file info
     *
     * @param fileid
     *
     * @return
     */
    @Headers("Accept: application/json")
    @GET("jobs/file/{fileid}")
    Call<JobFileItem> getJobFileInfo(
            @Path("fileid") String fileid
    );

    /**
     * List uploaded files for a job
     *
     * @param jobid     job id
     * @param fileState file state
     *
     * @return
     */
    @Headers("Accept: application/json")
    @GET("job/{jobid}/input/files")
    Call<JobFileItemList> listJobFiles(
            @Path("jobid") String jobid,
            @Query("fileState") String fileState,
            @Query("offset") int offset,
            @Query("max") int max
    );

    /**
     * List uploaded files for a job in state 'temp'
     *
     * @param jobid job id
     *
     * @return
     */
    @Headers("Accept: application/json")
    @GET("job/{jobid}/input/files")
    Call<JobFileItemList> listJobFiles(
            @Path("jobid") String jobid,
            @Query("offset") int offset,
            @Query("max") int max
    );

    /**
     * List uploaded files for an execution
     *
     * @param execid execution id
     *
     * @return
     */
    @Headers("Accept: application/json")
    @GET("execution/{execid}/input/files")
    Call<JobFileItemList> listExecutionFiles(
            @Path("execid") String execid,
            @Query("offset") int offset,
            @Query("max") int max
    );

    @Headers("Accept: application/json")
    @POST("job/{jobid}/input/file")
    Call<JobFileUploadResult> uploadJobOptionFile(
            @Path("jobid") String jobid,
            @Query("optionName") String optionName,
            @Query("fileName") String fileName,
            @Body RequestBody body
    );

    /**
     * enable execution for a job
     *
     * @param jobid
     *
     * @return
     */
    @Headers("Accept: application/json")
    @POST("job/{jobid}/execution/enable")
    Call<Simple> jobExecutionEnable(
            @Path("jobid") String jobid
    );

    /**
     * disable schedule for a job
     *
     * @param jobid
     *
     * @return
     */
    @Headers("Accept: application/json")
    @POST("job/{jobid}/execution/disable")
    Call<Simple> jobExecutionDisable(
            @Path("jobid") String jobid
    );

    /**
     * enable schedule for a job
     *
     * @param jobid
     *
     * @return
     */
    @Headers("Accept: application/json")
    @POST("job/{jobid}/schedule/enable")
    Call<Simple> jobScheduleEnable(
            @Path("jobid") String jobid
    );

    /**
     * disable schedule for a job
     *
     * @param jobid
     *
     * @return
     */
    @Headers("Accept: application/json")
    @POST("job/{jobid}/schedule/disable")
    Call<Simple> jobScheduleDisable(
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
     * @see <a href="http://rundeck.org/docs/api/#getting-project-info">API</a>
     */
    @Headers("Accept: application/json")
    @GET("project/{project}")
    Call<ProjectItem> getProjectInfo(@Path("project") String project);

    /**
     *
     * @see <a href="http://rundeck.org/docs/api/index.html#listing-resources">api</a>
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/resources")
    Call<Map<String, ProjectNode>> listNodes(@Path("project") String project, @Query("filter") String filter);

    /**
     * @see <a href="http://rundeck.org/docs/api/#put-project-configuration">api</a>
     */
    @Headers("Accept: application/json")
    @PUT("project/{project}/config")
    Call<ProjectConfig> setProjectConfiguration(@Path("project") String project, @Body ProjectConfig config);

    /**
     * @see <a href="http://rundeck.org/docs/api/#get-project-configuration">api</a>
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/config")
    Call<ProjectConfig> getProjectConfiguration(@Path("project") String project);

    /**
     * @see <a href="http://rundeck.org/docs/api/#put-project-configuration-key">api</a>
     */
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @PUT("project/{project}/config/{key}")
    Call<ProjectConfig> setProjectConfigurationKey(
            @Path("project") String project,
            @Path("key") String key,
            @Body ProjectConfig value
    );

    /**
     * @see <a href="http://rundeck.org/docs/api/#delete-project-configuration-key">api</a>
     */
    @DELETE("project/{project}/config/{key}")
    Call<Void> deleteProjectConfigurationKey(
            @Path("project") String project,
            @Path("key") String key
    );


    @Headers("Accept: application/json")
    @POST("projects")
    Call<ProjectItem> createProject(@Body ProjectItem properties);

    @DELETE("project/{project}")
    Call<Void> deleteProject(@Path("project") String project);


    /**
     * Export project archive (<=v18)
     *
     * @param project project
     * @param ids     option execution IDs, or null for all contents
     *
     * @return archive response
     */
    @Headers("Accept: application/zip")
    @GET("project/{project}/export")
    Call<ResponseBody> exportProject(
            @Path("project") String project,
            @Query("executionIds") List<String> ids
    );

    /**
     * Export project archive (>=v19)
     *
     * @param project
     * @param all
     * @param jobs
     * @param execs
     * @param configs
     * @param readmes
     * @param acls
     *
     * @return
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/export/async")
    Call<ProjectExportStatus> exportProjectAsync(
            @Path("project") String project,
            @Query("exportAll") boolean all,
            @Query("exportJobs") boolean jobs,
            @Query("exportExecutions") boolean execs,
            @Query("exportConfigs") boolean configs,
            @Query("exportReadmes") boolean readmes,
            @Query("exportAcls") boolean acls,
            @Query("exportScm") boolean scm
    );

    /**
     * Export project archive (>=v19)
     *
     * @param project
     * @param ids     option execution IDs, or null for all contents
     *
     * @return
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/export/async")
    Call<ProjectExportStatus> exportProjectAsync(
            @Path("project") String project,
            @Query("executionIds") List<String> ids
    );

    /**
     * Async project export status
     *
     * @param project
     * @param token
     *
     * @return
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/export/status/{token}")
    Call<ProjectExportStatus> exportProjectStatus(
            @Path("project") String project,
            @Path("token") String token
    );

    /**
     * Async project export download
     *
     * @param project
     * @param token
     *
     * @return
     */
    @Headers("Accept: application/zip")
    @GET("project/{project}/export/download/{token}")
    Call<ResponseBody> exportProjectDownload(
            @Path("project") String project,
            @Path("token") String token
    );

    /**
     * Export project archive (<=v18)
     *
     * @param project project
     *
     * @return archive response
     */
    @Headers("Accept: application/json")
    @PUT("project/{project}/import")
    Call<ProjectImportStatus> importProjectArchive(
            @Path("project") String project,
            @Query("jobUuidOption") String jobUuidOption,
            @Query("importExecutions") Boolean importExecutions,
            @Query("importConfig") Boolean importConfig,
            @Query("importACL") Boolean importACL,
            @Query("importScm") Boolean importScm,
            @Body RequestBody body
    );

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

    /**
     * Delete all executions for a job.
     */
    @Headers("Accept: application/json")
    @DELETE("job/{id}/executions")
    Call<BulkExecutionDeleteResponse> deleteAllJobExecutions(@Path("id") String id);

    @Headers("Accept: application/json")
    @GET("execution/{id}/abort")
    Call<AbortResult> abortExecution(@Path("id") String id);

    @Headers("Accept: application/json")
    @GET("execution/{id}")
    Call<Execution> getExecution(@Path("id") String id);

    @Headers("Accept: application/json")
    @GET("execution/{id}/state")
    Call<ExecutionStateResponse> getExecutionState(@Path("id") String id);

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

    /**
     * Get log output, with optional compacted results
     * @param id
     * @param offset
     * @param lastmod
     * @param maxlines
     * @param compacted
     * @return
     */
    @Headers("Accept: application/json")
    @GET("execution/{id}/output")
    Call<ExecOutput> getOutput(
            @Path("id") String id,
            @Query("offset") Long offset,
            @Query("lastmod") Long lastmod,
            @Query("maxlines") Long maxlines,
            @Query("compacted") Boolean compacted
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


    @Headers("Accept: application/json")
    @POST("system/executions/enable")
    Call<SystemMode> executionModeEnable();

    @Headers("Accept: application/json")
    @POST("system/executions/disable")
    Call<SystemMode> executionModeDisable();



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
     * Tell a Rundeck server in cluster mode to claim all scheduled jobs from another cluster server.
     *
     * @see <a href="https://rundeck.org/docs/api/#takeover-schedule-in-cluster-mode">API</a>
     */
    @Headers("Accept: application/json")
    @PUT("scheduler/takeover")
    Call<SchedulerTakeoverResult> takeoverSchedule(@Body SchedulerTakeover schedulerTakeover);


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
     * <a href="http://rundeck.org/docs/api/index.html#setup-scm-plugin-for-a-project">Setup SCM Config for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     *
     * @return result
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/scm/{integration}/status")
    Call<ScmProjectStatusResult> getScmProjectStatus(
            @Path("project") String project,
            @Path("integration") String integration
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#get-scm-plugin-input-fields">Get SCM Setup Inputs for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     * @param type        plugin type
     *
     * @return result
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/scm/{integration}/plugin/{type}/input")
    Call<ScmSetupInputsResult> getScmSetupInputs(
            @Path("project") String project,
            @Path("integration") String integration,
            @Path("type") String type
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#get-project-scm-action-input-fields">Get SCM Action Inputs for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     * @param action      plugin type
     *
     * @return result
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/scm/{integration}/action/{action}/input")
    Call<ScmActionInputsResult> getScmActionInputs(
            @Path("project") String project,
            @Path("integration") String integration,
            @Path("action") String action
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#get-project-scm-action-input-fields">Get SCM Action Inputs for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     * @param action      plugin type
     *
     * @return result
     */
    @Headers("Accept: application/json")
    @POST("project/{project}/scm/{integration}/action/{action}")
    Call<ScmActionResult> performScmAction(
            @Path("project") String project,
            @Path("integration") String integration,
            @Path("action") String action,
            @Body ScmActionPerform body
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#get-project-scm-action-input-fields">Get SCM Action Inputs for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     *
     * @return result
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/scm/{integration}/plugins")
    Call<ScmPluginsResult> listScmPlugins(
            @Path("project") String project,
            @Path("integration") String integration
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#enable-scm-plugin-for-a-project">Enable SCM Integration for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     * @param type        type
     *
     */
    @Headers("Accept: application/json")
    @POST("project/{project}/scm/{integration}/plugin/{type}/enable")
    Call<Void> enableScmPlugin(
            @Path("project") String project,
            @Path("integration") String integration,
            @Path("type") String type
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#enable-scm-plugin-for-a-project">Disable SCM Integration for a
     * Project</a>
     *
     * @param project     project
     * @param integration integration
     * @param type        type
     *
     */
    @Headers("Accept: application/json")
    @POST("project/{project}/scm/{integration}/plugin/{type}/disable")
    Call<Void> disableScmPlugin(
            @Path("project") String project,
            @Path("integration") String integration,
            @Path("type") String type
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
     * <a href="http://rundeck.org/docs/api/index.html#create-a-token">Create a Token</a>
     *
     * @return created token
     * @since v19
     */
    @Headers("Accept: application/json")
    @POST("tokens")
    Call<ApiToken> createToken(
            @Body CreateToken create
    );

    /**
     * <a href="http://rundeck.org/docs/api/index.html#list-tokens">List Tokens</a>
     *
     * @return list of tokens for a user
     *
     */
    @Headers("Accept: application/json")
    @GET("token/{id}")
    Call<ApiToken> getToken(
            @Path("id") String id
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


    /**
     * @see <a href="http://rundeck.org/docs/api/#get-another-user-profile">API</a>
     */
    @Headers("Accept: application/json")
    @GET("user/info/{user}")
    Call<User> getUserInfo(@Path("user") String user);

    /**
     * @see <a href="http://rundeck.org/docs/api/#get-user-profile">API</a>
     */
    @Headers("Accept: application/json")
    @GET("user/info/")
    Call<User> getUserInfo();

    /**
     * @see <a href="http://rundeck.org/docs/api/#modify-another-user-profile">API</a>
     */
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("user/info/{user}")
    Call<User> editUserInfo(@Path("user") String user,@Body User value);

    /**
     * @see <a href="http://rundeck.org/docs/api/#modify-user-profile">API</a>
     */
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("user/info/")
    Call<User> editUserInfo(@Body User value);


    /**
     * @see <a href="http://rundeck.org/docs/api/#list-users">API</a>
     */
    @Headers("Accept: application/json")
    @GET("user/list")
    Call<List<User>> listUsers();

    @Headers("Accept: application/json")
    @POST("job/{id}/retry/{eid}")
    Call<Execution> retryJob(
            @Path("id") String id,
            @Path("eid") String eid,
            @Body ExecRetry execRetry

    );

    /**
<<<<<<< HEAD
     * @see <a href="http://rundeck.org/docs/api/#list-plugins">API</a>
     */
    @Headers("Accept: application/json")
    @GET("plugins/list")
    Call<List<RepositoryArtifacts>> listPlugins();

    /**
     * @see <a href="http://rundeck.org/docs/api/#upload-plugins">API</a>
     */
    @Headers("Accept: application/json")
    @POST("plugins/upload")
    Call<ArtifactActionMessage> uploadPlugin(@Body RequestBody pluginBinary);

    /**
     * @see <a href="http://rundeck.org/docs/api/#install-plugins">API</a>
     */
    @Headers("Accept: application/json")
    @POST("plugins/install/{pluginId}")
    Call<ArtifactActionMessage> installPlugin(@Path("pluginId") String pluginId);

    /**
     * @see <a href="http://rundeck.org/docs/api/#uninstall-plugins">API</a>
     */
    @Headers("Accept: application/json")
    @POST("plugins/uninstall/{pluginId}")
    Call<ArtifactActionMessage> uninstallPlugin(@Path("pluginId") String pluginId);

    /* Bulk toggle job execution. */

    /**
     * @see <a href="https://rundeck.org/docs/api/#bulk-toggle-job-execution">API</a>
     */
    @Json
    @Headers("Accept: application/json")
    @POST("jobs/execution/enable")
    Call<BulkToggleJobExecutionResponse> bulkEnableJobs(
        @Body IdList ids
    );

    /**
     * @see <a href="https://rundeck.org/docs/api/#bulk-toggle-job-execution">API</a>
     */
    @Json
    @Headers("Accept: application/json")
    @POST("jobs/execution/disable")
    Call<BulkToggleJobExecutionResponse> bulkDisableJobs(
        @Body IdList ids
    );

    /* Bulk toggle job schedule. */

    /**
     * @see <a href="https://rundeck.org/docs/api/#bulk-toggle-job-schedules">API</a>
     */
    @Json
    @Headers("Accept: application/json")
    @POST("jobs/schedule/enable")
    Call<BulkToggleJobScheduleResponse> bulkEnableJobSchedule(
        @Body IdList ids
    );

    /**
     * @see <a href="https://rundeck.org/docs/api/#bulk-toggle-job-schedules">API</a>
     */
    @Json
    @Headers("Accept: application/json")
    @POST("jobs/schedule/disable")
    Call<BulkToggleJobScheduleResponse> bulkDisableJobSchedule(
        @Body IdList ids
    );

    /**
     * List reactions for a project
     *
     * @param project
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/events/reactions")
    Call<List<Reaction>> listReactions(
            @Path("project") String project
            /*,
            @Query("offset") int offset,
            @Query("max") int max*/
    );

    /**
     * Get a reaction by ID
     *
     * @param id
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/events/reactions/{id}")
    Call<Reaction> getReactionInfo(
            @Path("project") String project,
            @Path("id") String id
    );

    /**
     * Get reaction events by ID
     *
     * @param id
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/events/reactions/{id}/events")
    Call<ReactionEventList> getReactionEvents(
            @Path("project") String project,
            @Path("id") String id,
            @Query("offset") int offset,
            @Query("max") int max
    );

    /**
     * Create a reaction
     */
    @Headers("Accept: application/json")
    @POST("project/{project}/events/reactions")
    Call<Reaction> createReaction(
            @Path("project") String project,
            @Body Reaction reaction
    );
    /**
     * Create a reaction
     */
    @Headers("Accept: application/json")
    @PUT("project/{project}/events/reactions/{id}")
    Call<Reaction> updateReaction(
            @Path("project") String project,
            @Path("id") String id,
            @Body Reaction reaction
    );

    /**
     * Delete a reaction
     */
    @Headers("Accept: application/json")
    @DELETE("project/{project}/events/reactions/{id}")
    Call<Void> deleteReaction(
            @Path("project") String project,
            @Path("id") String id
    );


    /**
     * List event subscriptions for a project
     *
     * @param project
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/events/subscriptions")
    Call<List<Subscription>> listSubscriptions(
            @Path("project") String project
            /*,
            @Query("offset") int offset,
            @Query("max") int max*/
    );


    /**
     * Get a event subscriptions by ID
     *
     * @param id
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/events/subscriptions/{id}")
    Call<Subscription> getSubscriptionInfo(
            @Path("project") String project,
            @Path("id") String id
    );
    /**
     * Get a event subscriptions by ID
     *
     * @param id
     */
    @Headers("Accept: application/json")
    @GET("project/{project}/events/subscriptions/{id}/messages")
    Call<List<SubscriptionEventMessage>> getSubscriptionMessages(
            @Path("project") String project,
            @Path("id") String id
    );

    /**
     * Create a event subscriptions
     */
    @Headers("Accept: application/json")
    @POST("project/{project}/events/subscriptions")
    Call<Subscription> createSubscription(
            @Path("project") String project,
            @Body Subscription subscriptions
    );
    /**
     * Create a event subscriptions
     */
    @Headers("Accept: application/json")
    @PUT("project/{project}/events/subscriptions/{id}")
    Call<Subscription> updateSubscription(
            @Path("project") String project,
            @Path("id") String id,
            @Body Subscription subscriptions
    );

    /**
     * Delete a event subscriptions
     */
    @Headers("Accept: application/json")
    @DELETE("project/{project}/events/subscriptions/{id}")
    Call<Void> deleteSubscription(
            @Path("project") String project,
            @Path("id") String id
    );


    // Metrics calls
    /**
     * @see <a href="https://rundeck.org/docs/api/#list-metrics">API</a>
     */
    @Headers("Accept: application/json")
    @GET("metrics")
    Call<EndpointListResult> listMetricsEndpoints();

    /**
     * @see <a href="https://rundeck.org/docs/api/#metrics-healthcheck">API</a>
     */
    @Headers("Accept: application/json")
    @GET("metrics/healthcheck")
    Call<Map<String, HealthCheckStatus>> getHealthCheckMetrics();

    /**
     * @see <a href="https://rundeck.org/docs/api/#metrics-threads">API</a>
     */
    @Headers("Accept: application/json")
    @GET("metrics/threads")
    Call<ResponseBody> getThreadMetrics();

    /**
     * @see <a href="https://rundeck.org/docs/api/#metrics-ping">API</a>
     */
    @Headers("Accept: application/json")
    @GET("metrics/ping")
    Call<ResponseBody> getPing();

        /**
     * @see <a href="https://rundeck.org/docs/api/#metrics-data">API</a>
     */
    @Headers("Accept: application/json")
    @GET("metrics/metrics")
    Call<MetricsData> getMetricsData();

}
