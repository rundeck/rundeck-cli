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

package org.rundeck.client.util;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.rundeck.client.api.AuthorizationFailed;
import org.rundeck.client.api.RequestFailed;
import org.rundeck.client.api.model.ErrorDetail;
import org.rundeck.client.api.model.ErrorResponse;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * Holds Retrofit and a retrofit-constructed service
 */
public class Client<T> {

    public static final String APPLICATION_JSON = "application/json";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse(APPLICATION_JSON);
    public static final String APPLICATION_XML = "application/xml";
    public static final MediaType MEDIA_TYPE_XML = MediaType.parse(APPLICATION_XML);
    public static final String APPLICATION_ZIP = "application/zip";
    public static final MediaType MEDIA_TYPE_ZIP = MediaType.parse(APPLICATION_ZIP);
    public static final String APPLICATION_PGP_KEYS = "application/pgp-keys";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String APPLICATION_X_RUNDECK_PWORD_MIME_TYPE = "application/x-rundeck-data-password";
    public static final MediaType MEDIA_TYPE_GPG_KEYS = MediaType.parse(APPLICATION_PGP_KEYS);
    public static final MediaType MEDIA_TYPE_OCTET_STREAM = MediaType.parse(APPLICATION_OCTET_STREAM);
    public static final MediaType MEDIA_TYPE_X_RUNDECK_PASSWORD = MediaType.parse(APPLICATION_X_RUNDECK_PWORD_MIME_TYPE);
    public static final String APPLICATION_YAML = "application/yaml";
    public static final MediaType MEDIA_TYPE_YAML = MediaType.parse(APPLICATION_YAML);
    public static final MediaType MEDIA_TYPE_TEXT_YAML = MediaType.parse("text/yaml");
    public static final MediaType MEDIA_TYPE_TEXT_XML = MediaType.parse("text/xml");
    public static final String API_ERROR_API_VERSION_UNSUPPORTED = "api.error.api-version.unsupported";
    private T service;
    private Retrofit retrofit;
    private final int apiVersion;
    private final String appBaseUrl;
    private final String apiBaseUrl;
    private final boolean allowVersionDowngrade;
    private final Logger logger;

    public static interface Logger {
        void output(String out);

        void warning(String warn);

        void error(String err);
    }

    public Client(
            final T service,
            final Retrofit retrofit,
            final String appBaseUrl,
            final String apiBaseUrl,
            final int apiVersion,
            final boolean allowVersionDowngrade,
            final Logger logger
    )
    {
        this.service = service;
        this.retrofit = retrofit;
        this.appBaseUrl = appBaseUrl;
        this.apiBaseUrl = apiBaseUrl;
        this.apiVersion = apiVersion;
        this.allowVersionDowngrade = allowVersionDowngrade;
        this.logger = logger;
    }

    /**
     * @param body  body
     * @param types list of media types
     *
     * @return true if body has one of the media types
     */
    public static boolean hasAnyMediaType(final ResponseBody body, final MediaType... types) {
        MediaType mediaType1 = body.contentType();
        for (MediaType mediaType : types) {
            if (mediaType1.type().equals(mediaType.type()) && mediaType1.subtype().equals(mediaType.subtype())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Execute the remote call, and return the expected type if successful. if unsuccessful
     * throw an exception with relevant error detail
     *
     * @param execute call
     * @param <R>     expected result type
     *
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    public <R> R checkError(final Call<R> execute) throws IOException {
        Response<R> response = execute.execute();
        return checkError(response);
    }

    /**
     * Execute the remote call, and return the expected type if successful. if unsuccessful
     * throw an exception with relevant error detail
     *
     * @param execute call
     * @param <R>     expected result type
     *
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    public <R> R checkErrorDowngradable(final Call<R> execute) throws IOException, UnsupportedVersion {
        Response<R> response = execute.execute();
        return checkErrorDowngradable(response);
    }

    /**
     * @return the base URL used without API path
     */
    public String getAppBaseUrl() {
        return appBaseUrl;
    }

    /**
     * @return the API URL used
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public static final class UnsupportedVersion extends Exception {
        private final int requestedVersion;
        private final int latestVersion;
        private final RequestFailed requestFailed;

        public UnsupportedVersion(
                final String message,
                final RequestFailed cause,
                final int requestedVersion,
                final int latestVersion
        )
        {
            super(message, cause);
            this.requestFailed = getRequestFailed();
            this.requestedVersion = requestedVersion;
            this.latestVersion = latestVersion;
        }

        public int getLatestVersion() {
            return latestVersion;
        }

        public int getRequestedVersion() {
            return requestedVersion;
        }

        public RequestFailed getRequestFailed() {
            return requestFailed;
        }
    }

    /**
     * return the expected type if successful. if response is unsuccessful
     * throw an exception with relevant error detail
     *
     * @param response call response
     * @param <R>      expected type
     *
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    public <R> R checkError(final Response<R> response) throws IOException {
        if (!response.isSuccessful()) {
            return handleError(response, readError(response));
        }
        return response.body();
    }

    /**
     * return the expected type if successful. if response is unsuccessful
     * throw an exception with relevant error detail
     *
     * @param response call response
     * @param <R>      expected type
     *
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    public <R> R checkErrorDowngradable(final Response<R> response) throws IOException, UnsupportedVersion {
        if (!response.isSuccessful()) {
            ErrorDetail error = readError(response);
            checkUnsupportedVersion(response, error);
            return handleError(response, error);
        }
        return response.body();
    }

    private <R> R handleError(final Response<R> response, final ErrorDetail error) {
        reportApiError(error);
        throw makeErrorThrowable(response, error);

    }

    private <R> RequestFailed makeErrorThrowable(final Response<R> response, final ErrorDetail error) {
        if (response.code() == 401 || response.code() == 403) {
            //authorization
            return new AuthorizationFailed(
                    String.format("Authorization failed: %d %s", response.code(), response.message()),
                    response.code(),
                    response.message()
            );
        }
        if (response.code() == 409) {
            //authorization
            return new RequestFailed(String.format(
                    "Could not create resource: %d %s",
                    response.code(),
                    response.message()
            ), response.code(), response.message());
        }
        if (response.code() == 404) {
            //authorization
            return new RequestFailed(String.format(
                    "Could not find resource:  %d %s",
                    response.code(),
                    response.message()
            ), response.code(), response.message());
        }
        return new RequestFailed(
                String.format("Request failed:  %d %s", response.code(), response.message()),
                response.code(),
                response.message()
        );
    }

    public <R> void checkUnsupportedVersion(final Response<R> response, final ErrorDetail error)
            throws UnsupportedVersion
    {
        if (null != error && allowVersionDowngrade && API_ERROR_API_VERSION_UNSUPPORTED.equals(error.getErrorCode())) {
            throw new UnsupportedVersion(error.getErrorMessage(),
                                         makeErrorThrowable(response, error),
                                         getApiVersion(), error.getApiVersion()
            );
        }
    }

    public void reportApiError(final ErrorDetail error) {
        if (null != error) {
            logger.error(String.format("Error: %s", error));
            if (API_ERROR_API_VERSION_UNSUPPORTED.equals(error.getErrorCode())) {
                logger.warning(String.format(
                        "Note: You requested an API endpoint using an unsupported version.\n" +
                        "You can set a specific version by using a Rundeck URL in the format:\n" +
                        "  export RD_URL=%sapi/%s",
                        getAppBaseUrl(),
                        error.getApiVersion()
                ));
                logger.warning(
                        "You can enable auto-downgrading to a supported version: \n" +
                        "  export RD_API_DOWNGRADE=true"
                );
            }
        }
    }

    /**
     * Attempt to parse the response as a json or xml error and return the detail
     *
     * @param execute response
     *
     * @return parsed error detail or null if media types did not match
     *
     * @throws IOException if a parse error occurs
     */
    ErrorDetail readError(Response<?> execute) throws IOException {

        ResponseBody responseBody = execute.errorBody();
        if (hasAnyMediaType(responseBody, MEDIA_TYPE_JSON)) {
            Converter<ResponseBody, ErrorResponse> errorConverter = getRetrofit().responseBodyConverter(
                    ErrorResponse.class,
                    new Annotation[0]
            );
            return errorConverter.convert(responseBody);
        } else if (hasAnyMediaType(responseBody, MEDIA_TYPE_TEXT_XML, MEDIA_TYPE_XML)) {
            //specify xml annotation to parse as xml
            Annotation[] annotationsByType = ErrorResponse.class.getAnnotationsByType(Xml.class);
            Converter<ResponseBody, ErrorResponse> errorConverter = getRetrofit().responseBodyConverter(
                    ErrorResponse.class,
                    annotationsByType
            );
            return errorConverter.convert(responseBody);
        } else {
            return null;
        }
    }

    /**
     * If the response has one of the expected media types, parse into the error type
     *
     * @param execute    response
     * @param errorType  class of response
     * @param mediaTypes expected media types
     * @param <X>        error type
     *
     * @return error type instance, or null if mediate type does not match
     *
     * @throws IOException if media type matched, but parsing was unsuccessful
     */
    @SuppressWarnings("SameParameterValue")
    public <X> X readError(Response<?> execute, Class<X> errorType, MediaType... mediaTypes) throws IOException {

        ResponseBody responseBody = execute.errorBody();
        if (hasAnyMediaType(responseBody, mediaTypes)) {
            Converter<ResponseBody, X> errorConverter = getRetrofit().responseBodyConverter(
                    errorType,
                    new Annotation[0]
            );
            return errorConverter.convert(responseBody);
        } else {
            return null;
        }
    }

    /**
     * call a function using the service
     *
     * @param func function using the service
     * @param <U>  result type
     *
     * @return result
     *
     * @throws IOException if an error occurs
     */
    public <U> U apiCall(final Function<T, Call<U>> func) throws IOException {
        return checkError(func.apply(getService()));
    }

    /**
     * call a function using the service
     *
     * @param func function using the service
     * @param <U>  result type
     *
     * @return result
     *
     * @throws IOException if an error occurs
     */
    public <U> U apiCallDowngradable(final Function<T, Call<U>> func) throws IOException, UnsupportedVersion {
        return checkErrorDowngradable(func.apply(getService()));
    }

    public T getService() {
        return service;
    }

    public void setService(T service) {
        this.service = service;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public int getApiVersion() {
        return apiVersion;
    }

}
