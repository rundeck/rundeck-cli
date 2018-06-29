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

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * Holds Retrofit and a retrofit-constructed service
 */
public class Client<T> implements ServiceClient<T> {

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

    public interface Logger {
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
    @Override
    public <R> R checkError(final Call<R> execute) throws IOException {
        Response<R> response = execute.execute();
        return checkError(response);
    }

    /**
     * Execute the remote call, and return the expected type if successful. if unsuccessful
     * throw an exception with relevant error detail
     *
     * @param <R>     expected result type
     *
     * @param execute call
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    @Override
    public <R> WithErrorResponse<R> checkErrorResponse(final Call<R> execute) throws IOException {
        Response<R> response = execute.execute();
        return checkErrorResponse(response);
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
    @Override
    public <R> R checkErrorDowngradable(final Call<R> execute) throws IOException, UnsupportedVersionDowngrade {
        Response<R> response = execute.execute();
        return checkErrorDowngradable(response);
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
//    @Override
    public <R> WithErrorResponse<R> checkErrorResponseDowngradable(final Call<R> execute)
            throws IOException, UnsupportedVersionDowngrade
    {
        Response<R> response = execute.execute();
        return checkErrorResponseDowngradable(response);
    }

    /**
     * @return the base URL used without API path
     */
    @Override
    public String getAppBaseUrl() {
        return appBaseUrl;
    }

    /**
     * @return the API URL used
     */
    @Override
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public static final class UnsupportedVersionDowngrade extends Exception {
        private final int requestedVersion;
        private final int latestVersion;
        private final RequestFailed requestFailed;

        public UnsupportedVersionDowngrade(
                final String message,
                final RequestFailed cause,
                final int requestedVersion,
                final int latestVersion
        )
        {
            super(message, cause);
            this.requestFailed = cause;
            this.requestedVersion = requestedVersion;
            this.latestVersion = latestVersion;
        }

        public int getSupportedVersion() {
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
    @Override
    public <R> R checkError(final Response<R> response) throws IOException {
        if (!response.isSuccessful()) {
            handleError(response, readError(response));
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
    @Override
    public <R> R checkError(final WithErrorResponse<R> response) throws IOException {
        if (!response.getResponse().isSuccessful()) {
            handleError(response.getResponse(), readError(response.getErrorBody()));
        }
        return response.getResponse().body();
    }

    /**
     * return the expected type if successful. if response is unsuccessful
     * throw an exception with relevant error detail
     *
     * @param <R>      expected type
     * @param response call response
     *
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    @Override
    public <R> WithErrorResponse<R> checkErrorResponse(final Response<R> response) throws IOException {
        if (!response.isSuccessful()) {
            handleErrorResponse(response, readError(response));
        }
        return withErrorResponse(response, null);
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
    @Override
    public <R> R checkErrorDowngradable(final Response<R> response) throws IOException, UnsupportedVersionDowngrade {
        if (!response.isSuccessful()) {
            ErrorDetail error = readError(response);
            checkUnsupportedVersion(response, error);
            handleError(response, error);
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
//    @Override
    public <R> WithErrorResponse<R> checkErrorResponseDowngradable(final Response<R> response)
            throws IOException, UnsupportedVersionDowngrade
    {
        if (!response.isSuccessful()) {
            RepeatableResponse repeatResponse = repeatResponse(response);
            ErrorDetail error = readError(repeatResponse);
            checkUnsupportedVersion(response, error);
            return withErrorResponse(response, repeatResponse);
        }
        return withErrorResponse(response, null);
    }

    private <R> WithErrorResponse<R> withErrorResponse(final Response<R> response, final RepeatableResponse errorBody) {
        return new WithErrorResponse<R>() {
            @Override
            public Response<R> getResponse() {
                return response;
            }

            @Override
            public RepeatableResponse getErrorBody() throws IOException {
                return errorBody;
            }
        };
    }

    private <R> void handleError(final Response<R> response, final ErrorDetail error) {
        reportApiError(error);
        throw makeErrorThrowable(response, error);
    }

    private <R> void handleErrorResponse(final Response<R> response, final ErrorDetail error) {
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
                    "Could not find resource: %d %s",
                    response.code(),
                    response.message()
            ), response.code(), response.message());
        }
        return new RequestFailed(
                String.format("Request failed: %d %s", response.code(), response.message()),
                response.code(),
                response.message()
        );
    }

    public <R> void checkUnsupportedVersion(final Response<R> response, final ErrorDetail error)
            throws UnsupportedVersionDowngrade
    {
        if (null != error &&
            allowVersionDowngrade &&
            isUnsupportedVersionError(error) &&
            isDowngradableError(error)) {
            throw new UnsupportedVersionDowngrade(
                    error.getErrorMessage(),
                    makeErrorThrowable(response, error),
                    getApiVersion(),
                    error.getApiVersion()
            );
        }
    }

    @Override
    public void reportApiError(final ErrorDetail error) {
        if (null != error) {
            logger.error(String.format("Error: %s", error));
            if (isUnsupportedVersionError(error)) {
                logger.warning(String.format(
                        "Note: You requested an API endpoint using an unsupported version.\n" +
                        "You can set a specific version by using a Rundeck URL in the format:\n" +
                        "  export RD_URL=%sapi/%s",
                        getAppBaseUrl(),
                        error.getApiVersion()
                ));
                if (isDowngradableError(error)) {
                    logger.warning(
                            "You can enable auto-downgrading to a supported version: \n" +
                            "  export RD_API_DOWNGRADE=true"
                    );
                }
            }
        }
    }

    private boolean isDowngradableError(final ErrorDetail error) {
        return error.getApiVersion() < getApiVersion();
    }

    private boolean isUnsupportedVersionError(final ErrorDetail error) {
        return error.getErrorCode() != null &&
               error.getErrorCode().startsWith(API_ERROR_API_VERSION_UNSUPPORTED);
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
        return readError(repeatResponse(execute));
    }

    /**
     * Attempt to parse the response as a json or xml error and return the detail
     *
     * @param repeatResponse repeatable response
     *
     * @return parsed error detail or null if media types did not match
     *
     * @throws IOException if a parse error occurs
     */
    ErrorDetail readError(final RepeatableResponse repeatResponse) throws IOException {

        if (ServiceClient.hasAnyMediaType(repeatResponse.contentType(), MEDIA_TYPE_JSON)) {
            Converter<ResponseBody, ErrorResponse> errorConverter = getRetrofit().responseBodyConverter(
                    ErrorResponse.class,
                    new Annotation[0]
            );
            return errorConverter.convert(repeatResponse.repeatBody());
        } else if (ServiceClient.hasAnyMediaType(repeatResponse.contentType(), MEDIA_TYPE_TEXT_XML, MEDIA_TYPE_XML)) {
            //specify xml annotation to parse as xml
            Annotation[] annotationsByType = ErrorResponse.class.getAnnotationsByType(Xml.class);
            Converter<ResponseBody, ErrorResponse> errorConverter = getRetrofit().responseBodyConverter(
                    ErrorResponse.class,
                    annotationsByType
            );
            return errorConverter.convert(repeatResponse.repeatBody());
        } else {
            return null;
        }
    }

    /**
     * implements repeatable response body
     */
    static class RepeatResponse implements RepeatableResponse {
        ResponseBody responseBody;
        byte[] bufferedBody;

        public RepeatResponse(final ResponseBody responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        public ResponseBody repeatBody() throws IOException {
            if (null != bufferedBody) {
                return ResponseBody.create(this.responseBody.contentType(), bufferedBody);
            }

            bufferedBody = responseBody.bytes();
            return ResponseBody.create(this.responseBody.contentType(), bufferedBody);
        }
    }

    private RepeatableResponse repeatResponse(final Response<?> execute) throws IOException {
        ResponseBody responseBody = execute.errorBody();
        return new RepeatResponse(responseBody);
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
    @Override
    @SuppressWarnings("SameParameterValue")
    public <X> X readError(Response<?> execute, Class<X> errorType, MediaType... mediaTypes) throws IOException {
        return readError(repeatResponse(execute), errorType, mediaTypes);
    }

    /**
     * If the response has one of the expected media types, parse into the error type
     *
     * @param response   repeatable response body
     * @param errorType  class of response
     * @param mediaTypes expected media types
     * @param <X>        error type
     *
     * @return error type instance, or null if mediate type does not match
     *
     * @throws IOException if media type matched, but parsing was unsuccessful
     */
    @Override
    @SuppressWarnings("SameParameterValue")
    public <X> X readError(RepeatableResponse response, Class<X> errorType, MediaType... mediaTypes)
            throws IOException
    {

        ResponseBody responseBody = response.repeatBody();
        if (ServiceClient.hasAnyMediaType(responseBody.contentType(), mediaTypes)) {
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
    @Override
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
    @Override
    public <U> ServiceClient.WithErrorResponse<U> apiWithErrorResponse(final Function<T, Call<U>> func)
            throws IOException
    {
        return checkErrorResponse(func.apply(getService()));
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
    @Override
    public <U> U apiCallDowngradable(final Function<T, Call<U>> func) throws IOException, UnsupportedVersionDowngrade {
        return checkErrorDowngradable(func.apply(getService()));
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
    @Override
    public <U> WithErrorResponse<U> apiWithErrorResponseDowngradable(final Function<T, Call<U>> func)
            throws IOException, UnsupportedVersionDowngrade
    {
        return checkErrorResponseDowngradable(func.apply(getService()));
    }

    @Override
    public T getService() {
        return service;
    }

    public void setService(T service) {
        this.service = service;
    }

    @Override
    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public int getApiVersion() {
        return apiVersion;
    }

}
