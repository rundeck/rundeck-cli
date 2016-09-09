package org.rundeck.client;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.util.FormAuthInterceptor;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.QualifiedTypeConverterFactory;
import org.rundeck.client.util.StaticHeaderInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by greg on 3/28/16.
 */
public class Rundeck {
    public static final int API_VERS = 16;
    public static final Pattern API_VERS_PATTERN = Pattern.compile("^(.*)(/api/\\d+/?)$");

    /**
     * Create a client using the specified, or default version
     *
     * @param baseUrl
     * @param token
     * @param debugHttp
     *
     * @return
     */
    public static Client<RundeckApi> client(
            String baseUrl,
            final String token,
            final int debugHttp
    )
    {
        return client(baseUrl, API_VERS, token, debugHttp);
    }

    /**
     * Create a client using the specified, or default version
     *
     * @param baseUrl
     * @param username  username
     * @param password  pass
     * @param debugHttp
     *
     * @return
     */
    public static Client<RundeckApi> client(
            String baseUrl,
            final String username,
            final String password,
            final int debugHttp
    )
    {
        return client(baseUrl, API_VERS, username, password, debugHttp);
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
    public static Client<RundeckApi> client(
            String baseUrl,
            final int apiVers,
            final String authToken,
            final int httpLogging
    )
    {
        String base = buildApiUrlForVersion(baseUrl, apiVers);

        OkHttpClient.Builder callFactory = new OkHttpClient.Builder()
                .addInterceptor(new StaticHeaderInterceptor("X-Rundeck-Auth-Token", authToken));

        if (httpLogging > 0) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.values()[httpLogging %
                                                                   HttpLoggingInterceptor.Level.values().length]);
            callFactory.addInterceptor(logging);
        }
        Retrofit build = new Retrofit.Builder()
                .baseUrl(base)
                .callFactory(callFactory.build())
                .addConverterFactory(new QualifiedTypeConverterFactory(
                        JacksonConverterFactory.create(),
                        SimpleXmlConverterFactory.create(),
                        true
                ))
                .build();

        return new Client<>(build.create(RundeckApi.class), build);
    }

    /**
     * Create a client using the specified version if not set in URL
     *
     * @param baseUrl
     * @param apiVers
     * @param username
     * @param password
     * @param httpLogging
     *
     * @return
     */
    public static Client<RundeckApi> client(
            String baseUrl,
            final int apiVers,
            final String username,
            final String password,
            final int httpLogging
    )
    {

        String appBaseUrl = buildBaseAppUrlForVersion(baseUrl);
        String apiBaseUrl = buildApiUrlForVersion(baseUrl, apiVers);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        OkHttpClient.Builder callFactory = new OkHttpClient.Builder();
        String postUrl = HttpUrl.parse(baseUrl)
                                .newBuilder()
                                .addPathSegment(
                                        System.getProperty(
                                                "rundeck.client.j_security_check",
                                                "j_security_check"
                                        ))
                                .build()
                                .toString();
        callFactory.addInterceptor(new FormAuthInterceptor(
                username,
                password,
                appBaseUrl,
                postUrl,
                System.getProperty(
                        "rundeck.client.j_username",
                        "j_username"
                ),
                System.getProperty(
                        "rundeck.client.j_password",
                        "j_password"
                ),
                System.getProperty(
                        "rundeck.client.user.error",
                        "/user/error"
                )

        ));

        if (httpLogging > 0) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.values()[httpLogging %
                                                                   HttpLoggingInterceptor.Level.values().length]);
            callFactory.addInterceptor(logging);
        }

        callFactory.cookieJar(new JavaNetCookieJar(cookieManager));

        Retrofit build = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .callFactory(callFactory.build())
                .addConverterFactory(new QualifiedTypeConverterFactory(
                        JacksonConverterFactory.create(),
                        SimpleXmlConverterFactory.create(),
                        true
                ))
                .build();

        return new Client<>(build.create(RundeckApi.class), build);
    }

    /**
     * @param baseUrl input url
     * @param apiVers api VERSION to append if /api/VERS is not present
     *
     * @return URL for API by appending /api/VERS if it is not present
     */
    private static String buildApiUrlForVersion(String baseUrl, final int apiVers) {
        if (!baseUrl.matches("^.*/api/\\d+/?$")) {
            return baseUrl + "/api/" + (apiVers) + "/";
        } else if (!baseUrl.matches(".*/$")) {
            return baseUrl + "/";
        }
        return baseUrl;
    }

    /**
     * @param baseUrl input url
     *
     * @return the base part of the API url without the /api/VERS part
     */
    private static String buildBaseAppUrlForVersion(String baseUrl) {
        Matcher matcher = API_VERS_PATTERN.matcher(baseUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        } else if (!baseUrl.matches(".*/$")) {
            return baseUrl + "/";
        }
        return baseUrl;
    }
}
