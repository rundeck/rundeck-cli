package org.rundeck.client.tool.commands.enterprise.api;

import okhttp3.RequestBody;
import org.rundeck.client.tool.commands.enterprise.api.model.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface EnterpriseApi {
    @Headers("Accept: application/json")
    @GET("enterprise/license")
    Call<LicenseResponse> verifyLicense();

    @Headers("Accept: application/json")
    @POST("enterprise/license")
    Call<LicenseStoreResponse> storeLicense(@Body RequestBody contents, @Query("license_agreement") boolean agree);


    @Headers("Accept: application/json")
    @POST("enterprise/cluster/executions/enable")
    Call<EnterpriseModeResponse> executionModeEnable(@Query("uuid") String uuid);

    @Headers("Accept: application/json")
    @POST("enterprise/cluster/executions/disable")
    Call<EnterpriseModeResponse> executionModeDisable(@Query("uuid") String uuid);
    @Headers("Accept: application/json")
    @GET("project/{project}/execution/{id}/resumeStatus")
    Call<ResumeStatus> jobResumeStatus(@Path("project") String project, @Path("id") String executionId);

    @Headers("Accept: application/json")
    @POST("project/{project}/execution/{id}/resume")
    Call<ResumeResponse> resumeExecution(@Path("project") String project, @Path("id") String executionId);
}
