package org.rundeck.client.tool.commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import okhttp3.ResponseBody;
import org.rundeck.client.tool.InputError;
import org.rundeck.client.tool.extension.BaseCommand;
import org.rundeck.client.tool.options.VerboseOption;
import org.rundeck.client.util.DataOutput;
import picocli.CommandLine;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(
        description = "Authenticate via SSO provider",
        name = "auth",
        showEndOfOptionsDelimiterInUsageHelp = true,
        mixinStandardHelpOptions = true
)
public class Auth extends BaseCommand {

    @Getter
    @Setter
    @ToString
    public static class AuthOptions extends VerboseOption {
        @CommandLine.Option(names = {"-u", "--url"}, description = "Client URL", required = true)
        private String clientUrl;
        @CommandLine.Option(names = {"-i", "--id"}, description = "Client ID", required = true)
        private String clientId;
        @CommandLine.Option(
                names = {"-s", "--scope"},
                arity = "1..*",
                description = "Custom scope name",
                required = true
        )
        private List<String> scope;
        @CommandLine.Option(names = {"-e", "--clientSecretEnv"}, description = "Env Var to use for client secret")
        private String clientSecretEnv;
        @CommandLine.Option(
                names = {"-S", "--clientSecret"},
                description = "Client secret.",
                arity = "0..1",
                interactive = true
        )
        private char[] clientSecret;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OktaToken implements DataOutput {
        String access_token;
        String token_type;
        Integer expires_in;
        String scope;

        @Override
        public Map<?, ?> asMap() {
            return new HashMap<String, Object>() {{
                put("access_token", access_token);
                put("token_type", token_type);
                put("expires_in", expires_in);
                put("scope", scope);
            }};
        }
    }

    public static interface OktaApi {
        @Headers(
                "Accept: application/json"
        )
        @FormUrlEncoded
        @POST("oauth2/default/v1/token")
        retrofit2.Call<OktaToken> token(
                @Field("grant_type") String grantType,
                @Field("scope") String scope
        );
    }

    public static interface OktaApiProvider {
        OktaApi get(Auth.AuthOptions options, char[] clientSecret);
    }

    Executions.Interactive interactive = new Executions.ConsoleInteractive();
    OktaApiProvider oktaApiProvider = new DefaultOktaApiProvider();


    @CommandLine.Command(
            description = "Authenticate to Okta and acquire JWT token. If a client secret is not specified, user will" +
                          " be prompted for the secret.",
            name = "okta",
            mixinStandardHelpOptions = true
    )
    public int okta(@CommandLine.Mixin Auth.AuthOptions options) throws IOException, InputError {
        if (options.isVerbose()) {
            getRdOutput().info("Client URL: " + options.clientUrl);
            getRdOutput().info("Client ID: " + options.clientId);
            getRdOutput().info("Scope: " + options.scope);
        }
        char[] clientSecret = options.clientSecret;
        if (null == clientSecret && null != options.clientSecretEnv) {
            String getenv = System.getenv(options.clientSecretEnv);
            if (getenv != null) {
                clientSecret = getenv.toCharArray();
            }
        }
        if (null == clientSecret && interactive.isEnabled()) {
            clientSecret = interactive.readPassword("Enter client secret: ");
        }

        if (null == clientSecret) {
            getRdOutput().error(
                    "No user interaction available. Use --clientSecret or --clientSecretEnv to specify client secret");
            return 2;
        }

        OktaApi okta = oktaApiProvider.get(options, clientSecret);
        retrofit2.Call<OktaToken> clientCredentials = okta.token(
                "client_credentials",
                String.join(" ", options.scope)
        );

        if (options.isVerbose()) {
            getRdOutput().info("Generating JWT token...");
        }

        Response<OktaToken> execute = clientCredentials.execute();
        if (execute.isSuccessful()) {
            OktaToken body = execute.body();
            if (null == body) {
                getRdOutput().error("Unable to get response body");
                return 1;
            }
            if (options.isVerbose()) {
                getRdOutput().info("Token generated successfully.");
                getRdOutput().info("You can use the access_token value for the RD_AUTH environment variable:");
                getRdOutput().output(body);
            } else {
                getRdOutput().output(body.access_token);
            }
            return 0;
        } else {
            try (ResponseBody responseBody = execute.errorBody()) {
                String body = "(no response body)";
                if (null == responseBody) {
                    getRdOutput().error("Unable to get error response body");
                } else {
                    body = responseBody.string();
                }
                getRdOutput().error("Error: " + execute.code() + ": " + execute.message() + ": " + body);
                return 2;
            }
        }
    }


}
