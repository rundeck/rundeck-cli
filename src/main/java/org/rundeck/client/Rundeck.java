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
     * @return
     */
    public static RundeckApi client(String baseUrl, final String token) {
        return client(baseUrl, API_VERS, token);
    }

    /**
     * Create a client using the specified version if not set in URL
     *
     * @param baseUrl
     * @param apiVers
     * @param authToken
     *
     * @return
     */
    public static RundeckApi client(String baseUrl, final String apiVers, final String authToken) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        String base = buildBaseUrlForVersion(baseUrl, apiVers);
        System.err.println("base " + base);
        Retrofit build = new Retrofit.Builder()
                .baseUrl(base)
                .callFactory(new OkHttpClient.Builder().addInterceptor(
                        new StaticHeaderInterceptor(
                                "X-Rundeck-Auth-Token",
                                authToken
                        )).addInterceptor(
                        new StaticHeaderInterceptor(
                                "Accept",
                                "application/json"
                        ))
                                                       .addInterceptor(logging)
                                                       .build())
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
