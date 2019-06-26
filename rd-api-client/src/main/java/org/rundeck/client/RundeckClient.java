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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Build a {@link Client} for {@link RundeckApi} using {@link #builder()}.
 */
public class RundeckClient {
    public static final String USER_AGENT = Version.NAME + "/" + Version.VERSION;
    public static final int API_VERS = 29;
    public static final Pattern API_VERS_PATTERN = Pattern.compile("^(.*)(/api/(\\d+)/?)$");
    public static final String ENV_BYPASS_URL = "RD_BYPASS_URL";
    public static final String ENV_INSECURE_SSL = "RD_INSECURE_SSL";
    public static final String ENV_INSECURE_SSL_NO_WARN = "RD_INSECURE_SSL_NO_WARN";
    public static final String ENV_INSECURE_SSL_HOSTNAME = "RD_INSECURE_SSL_HOSTNAME";
    public static final String ENV_ALT_SSL_HOSTNAME = "RD_ALT_SSL_HOSTNAME";
    public static final String ENV_HTTP_TIMEOUT = "RD_HTTP_TIMEOUT";
    public static final String ENV_CONNECT_RETRY = "RD_CONNECT_RETRY";
    /**
     * If true, allow API version to be automatically degraded when unsupported version is detected
     */
    public static final String RD_API_DOWNGRADE = "RD_API_DOWNGRADE";
    public static final int INSECURE_SSL_LOGGING = 2;

    private RundeckClient() {
    }


    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        final OkHttpClient.Builder okhttp;
        String baseUrl;
        String appBaseUrl;
        int httpLogging;
        HttpUrl parseUrl;
        Integer apiVersion;
        boolean allowVersionDowngrade;
        Client.Logger logger;
        private String userAgent = USER_AGENT;

        Builder() {
            this.okhttp = new OkHttpClient.Builder();
        }

        <T> Builder accept(BuildWith<Builder, T> bw, T i) {
            bw.accept(this, i);
            return this;
        }

        public Builder config(RdClientConfig config) {
            logging(config.getDebugLevel());
            retryConnect(config.getBool(ENV_CONNECT_RETRY, true));
            timeout(config.getLong(ENV_HTTP_TIMEOUT, null));
            bypassUrl(config.getString(ENV_BYPASS_URL, null));
            insecureSSL(config.getBool(ENV_INSECURE_SSL, false));
            insecureSSLHostname(config.getBool(ENV_INSECURE_SSL_HOSTNAME, false));
            alternateSSLHostname(config.getString(ENV_ALT_SSL_HOSTNAME, null));
            allowVersionDowngrade(config.getBool(RD_API_DOWNGRADE, false));
            return this;
        }

        public Builder bypassUrl(final String string) {
            return accept(RundeckClient::configBypassUrl, string);
        }

        public Builder insecureSSL(final boolean bool) {
            return accept(RundeckClient::configInsecureSSL, bool);
        }

        public Builder insecureSSLHostname(final boolean bool) {
            return accept(RundeckClient::configInsecureSSLHostname, bool);
        }

        public Builder alternateSSLHostname(final String hostnames) {
            return accept(RundeckClient::configAlternateSSLHostname, hostnames);
        }

        public Builder retryConnect(final boolean bool) {
            return accept(RundeckClient::acceptRetry, bool);
        }

        public Builder timeout(final Long timeout) {
            return accept(RundeckClient::acceptTimeout, timeout);
        }

        public Builder baseUrl(final String baseUrl) {
            this.parseUrl = HttpUrl.parse(baseUrl);
            validateBaseUrl(baseUrl, parseUrl);
            this.baseUrl = baseUrl;
            this.appBaseUrl = buildBaseAppUrlForVersion(baseUrl);
            return this;
        }

        public Builder apiVersion(final int version) {
            this.apiVersion = version;
            return this;
        }

        public Builder allowVersionDowngrade(final boolean allow) {
            this.allowVersionDowngrade = allow;
            return this;
        }

        public Builder tokenAuth(final String authToken) {
            buildTokenAuth(okhttp, baseUrl, authToken);
            return this;
        }

        public Builder passwordAuth(final String username, final String password) {
            buildFormAuth(baseUrl, username, password, okhttp);
            return this;
        }

        public Client<RundeckApi> build() {
            return buildRundeckClient();
        }

        public Builder logging(final int p) {
            httpLogging = p;
            return accept(RundeckClient::configLogging, p);
        }

        public Builder logger(Client.Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Sets user agent string, the rd-api-client version will be appended
         *
         * @param ua user agent string
         */
        public Builder userAgent(String ua) {
            this.userAgent = getUserAgent(ua);
            return this;
        }

        public static String getUserAgent(final String info) {
            return String.format("%s (%s)", info, USER_AGENT);
        }

        private static void buildTokenAuth(
                final OkHttpClient.Builder builder,
                final String baseUrl,
                final String authToken
        )
        {
            HttpUrl parse = HttpUrl.parse(baseUrl);
            validateBaseUrl(baseUrl, parse);
            validateNotempty(authToken, "Token cannot be blank or null");
            builder.addInterceptor(new StaticHeaderInterceptor("X-Rundeck-Auth-Token", authToken));
        }

        private static void buildFormAuth(
                final String baseUrl,
                final String username,
                final String password, final OkHttpClient.Builder builder
        )
        {
            HttpUrl parse = HttpUrl.parse(baseUrl);
            validateBaseUrl(baseUrl, parse);
            validateNotempty(username, "User cannot be blank or null");
            validateNotempty(password, "Password cannot be blank or null");

            String appBaseUrl = buildBaseAppUrlForVersion(baseUrl);

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            builder.cookieJar(new JavaNetCookieJar(cookieManager));


            String postUrl = parse
                    .newBuilder()
                    .addPathSegment(
                            System.getProperty(
                                    "rundeck.client.j_security_check",
                                    "j_security_check"
                            ))
                    .build()
                    .toString();

            builder.addInterceptor(new FormAuthInterceptor(
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

        }

        private Client<RundeckApi> buildRundeckClient() {
            //url without version
            String appBaseUrl = buildBaseAppUrlForVersion(baseUrl);
            final String apiBaseUrl;
            if (null != apiVersion) {
                //construct api url using requested version
                apiBaseUrl = buildApiUrlForVersion(appBaseUrl, apiVersion);
            } else {
                //if url has no version, use default
                apiBaseUrl = buildApiUrlForVersion(baseUrl, API_VERS);
            }
            //detected final version
            int usedApiVers = apiVersionForUrl(apiBaseUrl, API_VERS);

            okhttp.addInterceptor(new StaticHeaderInterceptor("User-Agent", userAgent));


            Retrofit build = new Retrofit.Builder()
                    .baseUrl(apiBaseUrl)
                    .client(okhttp.build())
                    .addConverterFactory(new QualifiedTypeConverterFactory(
                            JacksonConverterFactory.create(),
                            SimpleXmlConverterFactory.create(),
                            true
                    ))
                    .build();

            return new Client<>(
                    build.create(RundeckApi.class),
                    build,
                    appBaseUrl,
                    apiBaseUrl,
                    usedApiVers,
                    allowVersionDowngrade,
                    logger
            );
        }

        private static void validateNotempty(final String authToken, final String s) {
            if ("".equals(authToken) || null == authToken) {
                throw new IllegalArgumentException(s);
            }
        }

        private static void validateBaseUrl(final String baseUrl, final HttpUrl parse) {
            if (null == parse) {
                throw new IllegalArgumentException("Not a valid base URL: " + baseUrl);
            }
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
    }

    interface BuildWith<T, X> {
        void accept(T builder, X val);
    }

    /**
     * @return new Builder
     */
    public static Builder builder() {
        return new Builder();
    }









    private static Builder configInsecureSSL(
            final Builder builder,
            final boolean insecureSsl
    )
    {
        if (insecureSsl) {
            SSLUtil.addInsecureSsl(builder.okhttp, builder.httpLogging);
        }
        return builder;
    }

    private static Builder configInsecureSSLHostname(
            final Builder builder,
            final boolean insecureSsl
    )
    {
        if (insecureSsl) {
            SSLUtil.addInsecureSSLHostnameVerifier(builder.okhttp, builder.httpLogging);
        }
        return builder;
    }

    private static Builder configAlternateSSLHostname(
            final Builder builder,
            final String value
    )
    {
        if (null != value) {
            List<String> collect = new ArrayList<>();
            collect.addAll(
                    Arrays.stream(value.split(", *"))
                          .map(String::trim)
                          .filter(s -> !"".equals(s))
                          .map(String::toUpperCase)
                          .collect(Collectors.toList())
            );

            List<String> names = Collections.unmodifiableList(collect);
            SSLUtil.addAlternateSSLHostnameVerifier(
                    builder.okhttp,
                    builder.httpLogging,
                    names
            );
        }
        return builder;
    }

    private static void configBypassUrl(
            final Builder builder,
            final String bypassUrl
    )
    {
        if (null != bypassUrl) {
            //fix redirects to external Rundeck URL by rewriting as to the baseurl
            builder.okhttp.addNetworkInterceptor(new RedirectBypassInterceptor(
                    builder.appBaseUrl,
                    normalizeUrlPath(bypassUrl)
            ));
        }
    }

    private static Builder acceptRetry(final Builder builder, final Boolean retryConnect) {
        if (null != retryConnect) {
            builder.okhttp.retryOnConnectionFailure(retryConnect);
        }
        return builder;
    }

    private static Builder acceptTimeout(final Builder builder, final Long timeout) {
        if (null != timeout) {
            builder.okhttp
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
            ;
        }
        return builder;
    }

    private static Builder configLogging(final Builder builder, final int httpLogging) {
        if (httpLogging > 0) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.values()[httpLogging %
                                                                   HttpLoggingInterceptor.Level.values().length]);
            builder.okhttp.addNetworkInterceptor(logging);
        }
        return builder;
    }


    /**
     * Normalize a url by appending a / if not present
     *
     * @param baseUrl
     *
     * @return
     */
    private static String normalizeUrlPath(String baseUrl) {
        if (!baseUrl.matches(".*/$")) {
            return baseUrl + "/";
        }
        return baseUrl;
    }

}
