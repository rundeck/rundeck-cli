package org.rundeck.client;

import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.rundeck.client.api.RundeckApi;
import org.rundeck.client.util.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by greg on 3/28/16.
 */
public class Rundeck {
    public static final int API_VERS = 16;
    public static final Pattern API_VERS_PATTERN = Pattern.compile("^(.*)(/api/(\\d+)/?)$");

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
            final int debugHttp,
            Long timeout,
            Boolean retryConnect
    )
    {
        return client(baseUrl, API_VERS, token, debugHttp, timeout, retryConnect);
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
            final int debugHttp,
            Long timeout,
            Boolean retryConnect
    )
    {
        return client(baseUrl, API_VERS, username, password, debugHttp, timeout, retryConnect);
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
            final int httpLogging,
            Long timeout,
            Boolean retryConnect
    )
    {
        HttpUrl parse = HttpUrl.parse(baseUrl);
        if (null == parse) {
            throw new IllegalArgumentException("Not a valid base URL: " + baseUrl);
        }
        if ("".equals(authToken) || null == authToken) {
            throw new IllegalArgumentException("Token cannot be blank or null");
        }
        String appBaseUrl = buildBaseAppUrlForVersion(baseUrl);
        String base = buildApiUrlForVersion(baseUrl, apiVers);
        int usedApiVers = apiVersionForUrl(baseUrl, apiVers);

        OkHttpClient.Builder callFactory = new OkHttpClient.Builder()
                .addInterceptor(new StaticHeaderInterceptor("X-Rundeck-Auth-Token", authToken));

        String bypassUrl = System.getProperty("rundeck.client.bypass.url", System.getenv("RUNDECK_BYPASS_URL"));

        if (null != bypassUrl) {
            //fix redirects to external Rundeck URL by rewriting as to the baseurl
            callFactory.addNetworkInterceptor(new RedirectBypassInterceptor(
                    appBaseUrl,
                    bypassUrl
            ));
        }
        if (httpLogging > 0) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.values()[httpLogging %
                                                                   HttpLoggingInterceptor.Level.values().length]);
            callFactory.addNetworkInterceptor(logging);
        }
        if (null != timeout) {
            callFactory
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
            ;
        }
        if (null != retryConnect) {
            callFactory.retryOnConnectionFailure(retryConnect);
        }
        Retrofit build = new Retrofit.Builder()
                .baseUrl(base)
                .client(callFactory.build())
                .addConverterFactory(new QualifiedTypeConverterFactory(
                        JacksonConverterFactory.create(),
                        SimpleXmlConverterFactory.create(),
                        true
                ))
                .build();

        return new Client<>(build.create(RundeckApi.class), build, usedApiVers);
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
            final int httpLogging,
            Long timeout,
            Boolean retryConnect
    )
    {

        HttpUrl parse = HttpUrl.parse(baseUrl);
        if (null == parse) {
            throw new IllegalArgumentException("Not a valid base URL: " + baseUrl);
        }
        if ("".equals(username) || null == username) {
            throw new IllegalArgumentException("User cannot be blank or null");
        }
        if ("".equals(password) || null == password) {
            throw new IllegalArgumentException("Password cannot be blank or null");
        }
        String appBaseUrl = buildBaseAppUrlForVersion(baseUrl);
        String apiBaseUrl = buildApiUrlForVersion(baseUrl, apiVers);
        int usedApiVers = apiVersionForUrl(baseUrl, apiVers);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        OkHttpClient.Builder callFactory = new OkHttpClient.Builder();
        String postUrl = parse
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
        String bypassUrl = System.getProperty("rundeck.client.bypass.url", System.getenv("RUNDECK_BYPASS_URL"));

        if (null != bypassUrl) {
            //fix redirects to external Rundeck URL by rewriting as to the baseurl
            callFactory.addNetworkInterceptor(new RedirectBypassInterceptor(
                    appBaseUrl,
                    normalizeUrlPath(bypassUrl)
            ));
        }
        if (httpLogging > 0) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.values()[httpLogging %
                                                                   HttpLoggingInterceptor.Level.values().length]);
            callFactory.addNetworkInterceptor(logging);
        }
        if (null != timeout) {
            callFactory
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
            ;
        }
        if (null != retryConnect) {
            callFactory.retryOnConnectionFailure(retryConnect);
        }

        callFactory.cookieJar(new JavaNetCookieJar(cookieManager));

        Retrofit build = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .client(callFactory.build())
                .addConverterFactory(new QualifiedTypeConverterFactory(
                        JacksonConverterFactory.create(),
                        SimpleXmlConverterFactory.create(),
                        true
                ))
                .build();

        return new Client<>(build.create(RundeckApi.class), build, usedApiVers);
    }

    /**
     * @param baseUrl input url
     * @param apiVers api VERSION to append if /api/VERS is not present
     *
     * @return URL for API by appending /api/VERS if it is not present
     */
    private static String buildApiUrlForVersion(String baseUrl, final int apiVers) {
        if (!baseUrl.matches("^.*/api/\\d+/?$")) {
            return normalizeUrlPath(baseUrl) + "api/" + (apiVers) + "/";
        }
        return normalizeUrlPath(baseUrl);
    }

    /**
     * @param baseUrl input url
     * @param apiVers api VERSION to append if /api/VERS is not present
     *
     * @return URL for API by appending /api/VERS if it is not present
     */
    private static int apiVersionForUrl(String baseUrl, final int apiVers) {
        Matcher matcher = API_VERS_PATTERN.matcher(baseUrl);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(3));
        }
        return apiVers;
    }

    private static String normalizeUrlPath(String baseUrl) {
        if (!baseUrl.matches(".*/$")) {
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
            return normalizeUrlPath(matcher.group(1));
        }
        return normalizeUrlPath(baseUrl);
    }
}
