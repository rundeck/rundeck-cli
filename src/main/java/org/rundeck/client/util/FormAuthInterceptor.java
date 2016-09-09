package org.rundeck.client.util;

import okhttp3.*;

import java.io.IOException;

/**
 * Handle Form authentication flow to Rundeck
 */
public class FormAuthInterceptor implements Interceptor {
    private boolean authorized;
    private String username;
    private String password;
    private String baseUrl;
    private String j_security_url;
    private String usernameField;
    private String passwordField;
    private String loginErrorURLPath;

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
        if (!execute.isSuccessful()) {
            throw new IllegalStateException(String.format("Expected successful response from: %s", baseUrl));
        }

        //now post username/password
        Response execute1 = chain.proceed(postAuthRequest());

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
