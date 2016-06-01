package org.rundeck.client.util;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.rundeck.client.api.AuthorizationFailed;
import org.rundeck.client.api.RequestFailed;
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
    public static final String APPLICATION_YAML = "application/yaml";
    public static final MediaType MEDIA_TYPE_YAML = MediaType.parse(APPLICATION_YAML);
    public static final MediaType MEDIA_TYPE_TEXT_YAML = MediaType.parse("text/yaml");
    public static final MediaType MEDIA_TYPE_TEXT_XML = MediaType.parse("text/xml");
    private T service;
    private Retrofit retrofit;

    public Client(final T service, final Retrofit retrofit) {
        this.service = service;
        this.retrofit = retrofit;
    }

    public static boolean hasAnyMediaType(final ResponseBody body, final MediaType... parse) {
        MediaType mediaType1 = body.contentType();
        for (MediaType mediaType : parse) {
            if (mediaType1.type().equals(mediaType.type()) && mediaType1.subtype().equals(mediaType.subtype())) {
                return true;
            }
        }
        return false;
    }

    public <T> T checkError(final Call<T> execute) throws IOException {
        Response<T> response = execute.execute();
        if (!response.isSuccessful()) {
            ErrorResponse error = readError(response);
            if (null != error) {
                System.err.printf("Error: %s%n", error);
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

    ErrorResponse readError(Response<?> execute) throws IOException {

        Converter<ResponseBody, ErrorResponse> errorConverter = getRetrofit().responseBodyConverter(
                ErrorResponse.class,
                new Annotation[0]
        );
        ResponseBody responseBody = execute.errorBody();
        if (hasAnyMediaType(responseBody, MEDIA_TYPE_JSON)) {
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
