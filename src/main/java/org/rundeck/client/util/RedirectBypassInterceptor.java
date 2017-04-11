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

import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

/**
 * Change redirect behavior, if Location begins with the bypass url, replace that part with the app URL.
 */
public class RedirectBypassInterceptor implements Interceptor {
    private final String appBaseUrl;
    private final String bypassUrl;

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
