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

import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;
import org.rundeck.client.RundeckClient;

import javax.net.ssl.*;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author greg
 * @since 4/5/17
 */
public class SSLUtil {
    /**
     * Add insecure trust manager and hostname verifier
     *
     * @param callFactory OkHttp builder
     * @param logging     logging level
     */
    public static void addInsecureSsl(final OkHttpClient.Builder callFactory, int logging) {
        addInsecureSSLTrustManager(callFactory, logging);
        addInsecureSSLHostnameVerifier(callFactory, logging);
    }

    public static void addInsecureSSLTrustManager(final OkHttpClient.Builder callFactory, final int logging) {
        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;
        try {
            trustManager = createInsecureSslTrustManager(logging);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        callFactory.sslSocketFactory(sslSocketFactory, trustManager);
    }

    /**
     * Trust all hostnames
     *
     * @param callFactory OkHttp builder
     * @param logging     logging level
     */
    public static void addInsecureSSLHostnameVerifier(final OkHttpClient.Builder callFactory, final int logging) {
        HostnameVerifier defaultVerifier = OkHostnameVerifier.INSTANCE;
        callFactory.hostnameVerifier((hostname, session) -> {
            if (defaultVerifier.verify(hostname, session)) {
                return true;
            }
            if (logging >= RundeckClient.INSECURE_SSL_LOGGING) {
                System.err.printf("INSECURE_SSL:hostnameVerifier: trust host: %s%n", hostname);
            }
            return true;
        });
    }

    /**
     * Add a hostname verifier which falls back to
     * an alternate list of hostnames if the original hostname
     * does not match the certificate
     *
     * @param callFactory OkHttp builder
     * @param logging     logging level
     */
    public static void addAlternateSSLHostnameVerifier(
            final OkHttpClient.Builder callFactory,
            final int logging,
            final List<String> alternateHostnames
    )
    {
        HostnameVerifier defaultVerifier = OkHostnameVerifier.INSTANCE;
        callFactory.hostnameVerifier((hostname, session) -> {
            boolean verify = defaultVerifier.verify(hostname, session);
            if (verify) {
                return true;
            }
            Optional<String> tested = alternateHostnames.stream()
                                                        .filter(h -> defaultVerifier.verify(h, session))
                                                        .findAny();
            if (tested.isPresent() && logging >= RundeckClient.INSECURE_SSL_LOGGING) {
                System.err.printf("INSECURE_SSL:hostnameVerifier: trust host: %s: as %s%n", hostname, tested.get());
            }
            return tested.isPresent();
        });

    }

    private static X509TrustManager createInsecureSslTrustManager(int logging)
            throws GeneralSecurityException
    {
        return new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] cArrr = new X509Certificate[0];
                return cArrr;
            }

            @Override
            public void checkServerTrusted(
                    final X509Certificate[] chain,
                    final String authType
            ) throws CertificateException
            {
                if (logging >= RundeckClient.INSECURE_SSL_LOGGING) {
                    System.err.printf("INSECURE_SSL:TrustManager:checkServerTrusted: %s: chain: %s%n", authType,
                                      Arrays.toString(chain)
                    );
                }
            }

            @Override
            public void checkClientTrusted(
                    final X509Certificate[] chain,
                    final String authType
            ) throws CertificateException
            {
            }
        };
    }
}
