package org.rundeck.client.tool.commands.enterprise.api;

import org.rundeck.client.tool.commands.enterprise.api.model.LicenseResponse;
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseStoreResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface EnterpriseApi {
    @Headers("Accept: application/json")
    @GET("enterprise/license")
    Call<LicenseResponse> verifyLicense();

    @Headers("Accept: application/json")
    @POST("enterprise/license")
    Call<LicenseStoreResponse> storeLicense(@Body RequestBody contents, @Query("license_agreement") boolean agree);
}
