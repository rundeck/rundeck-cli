package org.rundeck.client.util;

import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

/**
 * Created by greg on 3/28/16.
 */
public class StaticHeaderInterceptor implements Interceptor {
    String name;
    String value;

    public StaticHeaderInterceptor(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder().header(name, value).build());
    }

}
