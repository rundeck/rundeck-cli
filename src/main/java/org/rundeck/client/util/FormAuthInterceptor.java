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
import org.rundeck.client.api.AuthorizationFailed;
import org.rundeck.client.api.LoginFailed;

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
        this.loginErrorURLPath = loginErrorPath;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        if (!authorized) {
            authenticate(chain);
        }

        return chain.proceed(chain.request());
    }

    /**
     * Retrieve base url, then subsequently post the authorization credentials
     */
    private void authenticate(final Chain chain) throws IOException {
        Response baseResponse = chain.proceed(baseUrlRequest());
        try {
            if (!baseResponse.isSuccessful()) {
                throw new IllegalStateException(String.format("Expected successful response from: %s", baseUrl));
            }
        } finally {
            baseResponse.body().close();
        }

        //now post username/password
        Response authResponse = chain.proceed(postAuthRequest());
        try {
            if (!authResponse.isSuccessful()) {
                throw new IllegalStateException("Password Authentication failed, expected a successful response.");
            }
            if (authResponse.request().url().toString().contains(loginErrorURLPath)) {
                //jetty behavior: redirect to login error page
                throw new LoginFailed(String.format("Password Authentication failed for: %s", username));
            }
            if (null == authResponse.priorResponse() && ServiceClient.hasAnyMediaType(
                    authResponse.body(),
                    MediaType.parse("text/html")
            )) {
                String securitycheck = System.getProperty(
                        "rundeck.client.j_security_check",
                        "j_security_check"
                );
                //tomcat behavior: render error page content without redirect
                //look for login form indicating login was not successful
                if (authResponse.body().string().contains("action=\"" + securitycheck + "\"")) {
                    throw new LoginFailed(String.format("Password Authentication failed for: %s", username));
                }
            }
        } finally {
            authResponse.body().close();
        }
        authorized = true;
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
