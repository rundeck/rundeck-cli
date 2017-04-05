package org.rundeck.client.util;

import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;
import org.rundeck.client.Rundeck;

import javax.net.ssl.*;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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
            if (logging >= Rundeck.INSECURE_SSL_LOGGING) {
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
            if (tested.isPresent() && logging >= Rundeck.INSECURE_SSL_LOGGING) {
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
                if (logging >= Rundeck.INSECURE_SSL_LOGGING) {
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
