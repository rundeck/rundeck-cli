package org.rundeck.client.util;

import okhttp3.OkHttpClient;
import org.rundeck.client.Rundeck;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * @author greg
 * @since 4/5/17
 */
public class SSLUtil {
    public static void addInsecureSsl(final OkHttpClient.Builder callFactory, int logging) {
        addInsecureSSLTrustManager(callFactory, logging);
        addInsecureSSLHostnameVerifier(callFactory, logging);
    }

    private static void addInsecureSSLTrustManager(final OkHttpClient.Builder callFactory, final int logging) {
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

    private static void addInsecureSSLHostnameVerifier(final OkHttpClient.Builder callFactory, final int logging) {
        callFactory.hostnameVerifier((hostname, session) -> {
            if (logging >= Rundeck.INSECURE_SSL_LOGGING) {
                System.err.println("INSECURE_SSL:hostnameVerifier: trust host: " + hostname);
            }
            return true;
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
