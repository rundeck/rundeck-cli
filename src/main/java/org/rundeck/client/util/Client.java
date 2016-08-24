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

/**
 * Holds Retrofit and a retrofit-constructed service
 */
public class Client<T> {

    public static final String APPLICATION_JSON = "application/json";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse(APPLICATION_JSON);
    public static final String APPLICATION_XML = "application/xml";
    public static final MediaType MEDIA_TYPE_XML = MediaType.parse(APPLICATION_XML);
    public static final String APPLICATION_PGP_KEYS = "application/pgp-keys";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String APPLICATION_X_RUNDECK_PASSWORD = "application/x-rundeck-data-password";
    public static final MediaType MEDIA_TYPE_GPG_KEYS = MediaType.parse(APPLICATION_PGP_KEYS);
    public static final MediaType MEDIA_TYPE_OCTET_STREAM = MediaType.parse(APPLICATION_OCTET_STREAM);
    public static final MediaType MEDIA_TYPE_X_RUNDECK_PASSWORD = MediaType.parse(APPLICATION_X_RUNDECK_PASSWORD);
    public static final String APPLICATION_YAML = "application/yaml";
    public static final MediaType MEDIA_TYPE_YAML = MediaType.parse(APPLICATION_YAML);
    public static final MediaType MEDIA_TYPE_TEXT_YAML = MediaType.parse("text/yaml");
    public static final MediaType MEDIA_TYPE_TEXT_XML = MediaType.parse("text/xml");
    public static final String API_ERROR_API_VERSION_UNSUPPORTED = "api.error.api-version.unsupported";
    private T service;
    private Retrofit retrofit;

    public Client(final T service, final Retrofit retrofit) {
        this.service = service;
        this.retrofit = retrofit;
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
     * @param <T>     expected result type
     *
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    public <T> T checkError(final Call<T> execute) throws IOException {
        Response<T> response = execute.execute();
        return checkError(response);
    }

    /**
     * return the expected type if successful. if response is unsuccessful
     * throw an exception with relevant error detail
     *
     * @param response call response
     * @param <T>      expected type
     *
     * @return result
     *
     * @throws IOException if remote call is unsuccessful or parsing error occurs
     */
    public <T> T checkError(final Response<T> response) throws IOException {
        if (!response.isSuccessful()) {
            ErrorDetail error = readError(response);
            if (null != error) {
                System.err.printf("Error: %s%n", error);
                if (API_ERROR_API_VERSION_UNSUPPORTED.equals(error.getErrorCode())) {
                    System.err.printf("Note: You requested an API endpoint using an unsupported version. \n" +
                              "You can set a specific version by using a Rundeck " +
                              "URL in the format:\n" +
                              "  [RUNDECK_BASE_URL]/api/%s\n\n", error.getApiVersion());
                }
            }
            if (response.code() == 401 || response.code() == 403) {
                //authorization
                throw new AuthorizationFailed(
                        String.format("Authorization failed: %d %s", response.code(), response.message()),
                        response.code(),
                        response.message()
                );
            }
            if (response.code() == 409) {
                //authorization
                throw new RequestFailed(String.format(
                        "Could not create resource: %d %s",
                        response.code(),
                        response.message()
                ), response.code(), response.message());
            }
            if (response.code() == 404) {
                //authorization
                throw new RequestFailed(String.format(
                        "Could not find resource:  %d %s",
                        response.code(),
                        response.message()
                ), response.code(), response.message());
            }
            throw new RequestFailed(
                    String.format("Request failed:  %d %s", response.code(), response.message()),
                    response.code(),
                    response.message()
            );
        }
        return response.body();
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
}
