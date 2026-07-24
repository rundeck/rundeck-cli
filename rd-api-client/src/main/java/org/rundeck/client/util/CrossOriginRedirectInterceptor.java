/*
 * Copyright 2024 Rundeck, Inc. (http://rundeck.com)
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

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Network interceptor that blocks cross-origin HTTP redirects to prevent credential exfiltration.
 *
 * <p>When the server responds with a 3xx redirect whose {@code Location} header resolves to a
 * different scheme, host, or port than the original request, this interceptor throws an
 * {@link IOException} to abort the request rather than following the redirect with authentication
 * credentials attached.</p>
 *
 * <p>Cross-origin redirect following can be re-enabled by setting
 * {@code RD_ALLOW_CROSS_ORIGIN_REDIRECT=true} or passing {@code --allow-cross-origin-redirect}
 * on the command line.</p>
 */
public class CrossOriginRedirectInterceptor implements Interceptor {

    private static final Set<Integer> REDIRECT_CODES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(301, 302, 303, 307, 308))
    );

    private final boolean allowCrossOriginRedirect;

    /**
     * @param allowCrossOriginRedirect when {@code true}, cross-origin redirects are followed
     *                                 with credentials (original behaviour); when {@code false}
     *                                 (default), they are blocked with an {@link IOException}
     */
    public CrossOriginRedirectInterceptor(final boolean allowCrossOriginRedirect) {
        this.allowCrossOriginRedirect = allowCrossOriginRedirect;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (!allowCrossOriginRedirect && REDIRECT_CODES.contains(response.code())) {
            String location = response.header("Location");
            if (location != null) {
                HttpUrl requestUrl = chain.request().url();
                HttpUrl redirectUrl = requestUrl.resolve(location);
                if (redirectUrl != null && !isSameOrigin(requestUrl, redirectUrl)) {
                    response.close();
                    throw new IOException(
                            "Cross-origin redirect blocked for security: " + redirectUrl
                            + ". To allow cross-origin redirects set RD_ALLOW_CROSS_ORIGIN_REDIRECT=true"
                            + " or pass --allow-cross-origin-redirect."
                    );
                }
            }
        }
        return response;
    }

    /**
     * Returns {@code true} if both URLs share the same scheme, host, and port.
     */
    static boolean isSameOrigin(final HttpUrl url1, final HttpUrl url2) {
        return url1.scheme().equals(url2.scheme())
                && url1.host().equals(url2.host())
                && url1.port() == url2.port();
    }
}
