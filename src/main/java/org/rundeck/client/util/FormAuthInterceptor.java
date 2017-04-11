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

import okhttp3.*;

import java.io.IOException;

/**
 * Handle Form authentication flow to Rundeck
 */
public class FormAuthInterceptor implements Interceptor {
    private boolean authorized;
    private final String username;
    private final String password;
    private final String baseUrl;
    private final String j_security_url;
    private final String usernameField;
    private final String passwordField;
    private final String loginErrorURLPath;

    public FormAuthInterceptor(
            final String username,
            final String password,
            final String baseUrl,
            final String securityUrl,
            final String usernameField,
            final String passwordField,
            final String loginErrorPath
    )
    {
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
        this.j_security_url = securityUrl;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        loginErrorURLPath = loginErrorPath;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Response origResponse = chain.proceed(chain.request());
        if (origResponse.isSuccessful() || authorized) {
            return origResponse;
        }
        origResponse.body().close();
        //not authorized, not a successful result, attempt to authenticate
        return authenticate(chain);
    }

    /**
     * Retrieve base url, then subsequently post the authorization credentials
     *
     * @param chain
     *
     * @return
     *
     * @throws IOException
     */
    private Response authenticate(final Chain chain) throws IOException {
        Response execute = chain.proceed(baseUrlRequest());
        execute.body().close();
        if (!execute.isSuccessful()) {
            throw new IllegalStateException(String.format("Expected successful response from: %s", baseUrl));
        }

        //now post username/password
        Response execute1 = chain.proceed(postAuthRequest());
        execute1.body().close();
        if (!execute1.isSuccessful() || execute1.request().url().toString().contains(loginErrorURLPath)) {
            throw new IllegalStateException("Password Authentication failed, expected a successful response.");
        }
        authorized = true;


        //now retry original request
        return chain.proceed(chain.request());
    }

    private Request postAuthRequest() {
        return new Request.Builder()
                .url(j_security_url)
                .post(new FormBody.Builder()
                              .add(usernameField, username)
                              .add(passwordField, password)
                              .build())
                .build();
    }

    private Request baseUrlRequest() {
        return new Request.Builder().url(baseUrl).build();
    }

}
