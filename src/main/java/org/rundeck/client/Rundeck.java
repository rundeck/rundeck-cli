package org.rundeck.client;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.util.StaticHeaderInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by greg on 3/28/16.
 */
public class Rundeck {
    public static final String API_VERS = "16";

    /**
     * Create a client using the specified, or default version
     *
     * @param baseUrl
     * @param token
     *
     * @param debugHttp
     * @return
     */
    public static RundeckApi client(String baseUrl, final String token, final boolean debugHttp) {
        return client(baseUrl, API_VERS, token, debugHttp);
    }

    /**
     * Create a client using the specified version if not set in URL
     *
     * @param baseUrl
     * @param apiVers
     * @param authToken
     * @param httpLogging
     *
     * @return
     */
    public static RundeckApi client(
            String baseUrl,
            final String apiVers,
            final String authToken, final boolean httpLogging
    )
    {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        String base = buildBaseUrlForVersion(baseUrl, apiVers);

        OkHttpClient.Builder callFactory = new OkHttpClient.Builder().addInterceptor(
                new StaticHeaderInterceptor(
                        "X-Rundeck-Auth-Token",
                        authToken
                )).addInterceptor(
                new StaticHeaderInterceptor(
                        "Accept",
                        "application/json"
                ));
        if (httpLogging) {
            callFactory.addInterceptor(logging);
        }
        Retrofit build = new Retrofit.Builder()
                .baseUrl(base)
                .callFactory(callFactory.build())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return build.create(RundeckApi.class);
    }

    private static String buildBaseUrlForVersion(String baseUrl, final String apiVers) {
        if (!baseUrl.matches("^.*/api/\\d+/?$")) {
            return baseUrl + "/api/" + apiVers + "/";
        } else if (!baseUrl.matches(".*/$")) {
            return baseUrl + "/";
        }
        return baseUrl;
    }
}
