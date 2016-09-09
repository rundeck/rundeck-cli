package org.rundeck.client.util;

import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

/**
 * Change redirect behavior, if Location begins with the bypass url, replace that part with the app URL.
 */
public class RedirectBypassInterceptor implements Interceptor {
    private String appBaseUrl;
    private String bypassUrl;

    public RedirectBypassInterceptor(final String appBaseUrl, final String bypassUrl) {
        this.appBaseUrl = appBaseUrl;
        this.bypassUrl = bypassUrl;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        if (originalResponse.isRedirect()) {
            String originalLocation = originalResponse.header("location");
            String newUrl = remapUrl(originalLocation, bypassUrl, appBaseUrl);
            if (null != newUrl) {
                return originalResponse.newBuilder()
                                       .header("Location", newUrl)
                                       .build();
            }
        }
        return originalResponse;
    }

    /**
     * Replace the prefix of the originurl that starts with the bypassurl string with the appbaseurl string
     *
     * @param origUrl
     * @param bypassUrl
     * @param appBaseUrl
     *
     * @return
     */
    public static String remapUrl(final String origUrl, final String bypassUrl, final String appBaseUrl) {
        if (origUrl.startsWith(bypassUrl)) {
            return appBaseUrl + origUrl.substring(bypassUrl.length());
        }
        return null;
    }
}
