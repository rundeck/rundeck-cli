package org.rundeck.client.tool.commands;

import okhttp3.OkHttpClient;
import org.rundeck.client.util.StaticHeaderInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class DefaultOktaApiProvider implements Auth.OktaApiProvider {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BASIC = "Basic ";

    @Override
    public Auth.OktaApi get(Auth.AuthOptions options, char[] clientSecret) {
        Retrofit rfit = new Retrofit.Builder()
                .baseUrl(options.getClientUrl())
                .client(
                        new OkHttpClient.Builder()
                                .addInterceptor(new StaticHeaderInterceptor(
                                        AUTHORIZATION,
                                        BASIC + basicAuthString(options.getClientId(), clientSecret)
                                ))
                                .build()
                )
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return rfit.create(Auth.OktaApi.class);
    }

    /**
     * Create basic auth header value
     *
     * @param clientId     client ID
     * @param clientSecret client secret
     * @return encoded header value
     */
    public static String basicAuthString(String clientId, char[] clientSecret) {
        byte[] idBytes = clientId.getBytes(StandardCharsets.UTF_8);

        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(clientSecret));
        byte[] secretBytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());

        byte[] buffer = new byte[idBytes.length + secretBytes.length + 1];
        System.arraycopy(idBytes, 0, buffer, 0, idBytes.length);
        buffer[idBytes.length] = ':';
        System.arraycopy(secretBytes, 0, buffer, idBytes.length + 1, secretBytes.length);

        return new String(Base64.getEncoder().encode(buffer));
    }
}
