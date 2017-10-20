package org.rundeck.client.util;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.rundeck.client.api.model.ErrorDetail;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author greg
 * @since 10/19/17
 */
public interface ServiceClient<T> {
    /**
     * @param body  body
     * @param types list of media types
     *
     * @return true if body has one of the media types
     */
    static boolean hasAnyMediaType(ResponseBody body, MediaType... types) {
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
    <R> R checkError(Call<R> execute) throws IOException;

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
    <R> R checkErrorDowngradable(Call<R> execute) throws IOException, Client.UnsupportedVersion;

    /**
     * @return the base URL used without API path
     */
    String getAppBaseUrl();

    /**
     * @return the API URL used
     */
    String getApiBaseUrl();

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
    <R> R checkError(Response<R> response) throws IOException;

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
    <R> R checkErrorDowngradable(Response<R> response) throws IOException, Client.UnsupportedVersion;

    void reportApiError(ErrorDetail error);

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
    <X> X readError(Response<?> execute, Class<X> errorType, MediaType... mediaTypes) throws IOException;

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
    <U> U apiCall(Function<T, Call<U>> func) throws IOException;

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
    <U> U apiCallDowngradable(Function<T, Call<U>> func) throws IOException, Client.UnsupportedVersion;

    T getService();

    Retrofit getRetrofit();

    int getApiVersion();
}
