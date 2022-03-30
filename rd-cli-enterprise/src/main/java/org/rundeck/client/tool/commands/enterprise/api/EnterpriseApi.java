package org.rundeck.client.tool.commands.enterprise.api;

import okhttp3.RequestBody;
import org.rundeck.client.tool.commands.enterprise.api.model.EnterpriseModeResponse;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseResponse;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseStoreResponse;
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
}
