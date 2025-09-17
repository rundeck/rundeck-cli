package org.rundeck.client.tool.commands

import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

class AuthSpec extends Specification {

    def "auth successful response"() {
        given:
        Auth auth = createAuth()
        def clientId = 'clientId'
        def clientUrl = 'clientUrl'
        def clientSecret = 'clientSecret'
        def options = new Auth.AuthOptions()
        options.clientId = clientId
        options.clientUrl = clientUrl
        options.clientSecret = clientSecret.toCharArray()
        options.scope = ['testScope1']

        def tokenResult = new Auth.OktaToken()
        tokenResult.access_token = 'fakeAccesstoken'
        tokenResult.token_type = 'bearer'
        tokenResult.expires_in = 3600
        tokenResult.scope = 'testScope1'

        auth.oktaApiProvider = Mock(Auth.OktaApiProvider) {
            1 * get(options, _) >> Mock(Auth.OktaApi) {
                1 * token('client_credentials', 'testScope1') >> Calls.response(tokenResult)
                0 * _(*_)
            }
        }
        when:
        def result = auth.okta(options)
        then:
        result == 0
        0 * auth.rdOutput.error(*_)
        1 * auth.rdOutput.output('fakeAccesstoken')
    }

    def "auth request"() {
        given:

        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody('''{
    "token_type": "bearer",
    "expires_in": 3600,
    "access_token": "fakeAccesstoken",
    "scope": "testScope1"
}'''))
        server.start()
        Auth auth = createAuth()
        def options = new Auth.AuthOptions()
        options.clientId = 'asdf'
        options.clientUrl = server.url("/").toString()
        options.clientSecret = 'asdf'.toCharArray()
        options.scope = ['testScope1']

        when:
        def result = auth.okta(options)
        then:
        result == 0
        0 * auth.rdOutput.error(*_)
        1 * auth.rdOutput.output('fakeAccesstoken')
        RecordedRequest request1 = server.takeRequest()
        request1.path == "/oauth2/default/v1/token"
        request1.method == "POST"
        request1.body.readUtf8() == "grant_type=client_credentials&scope=testScope1"
        request1.headers.get('Authorization') == 'Basic YXNkZjphc2Rm'
        request1.headers.get('Accept') == 'application/json'
        cleanup:

        server.shutdown()
    }

    def "auth failure"() {
        given:
        Auth auth = createAuth()
        def clientId = 'clientId'
        def clientUrl = 'clientUrl'
        def clientSecret = 'clientSecret'
        def options = new Auth.AuthOptions()
        options.clientId = clientId
        options.clientUrl = clientUrl
        options.clientSecret = clientSecret.toCharArray()
        options.scope = ['testScope1']

        def tokenResult = new Auth.OktaToken()
        tokenResult.access_token = 'fakeAccesstoken'
        tokenResult.token_type = 'bearer'
        tokenResult.expires_in = 3600
        tokenResult.scope = 'testScope1'
        def errorJson = '{"error":"invalid_client","error_description":"The client secret supplied for a confidential client is invalid."}'
        auth.oktaApiProvider = Mock(Auth.OktaApiProvider) {
            1 * get(options, _) >> Mock(Auth.OktaApi) {
                1 * token('client_credentials', 'testScope1') >> Calls.response(
                        Response.error(
                                401,
                                ResponseBody.create(
                                        errorJson,
                                        Client.MEDIA_TYPE_JSON
                                )
                        )
                )
                0 * _(*_)
            }
        }
        when:
        def result = auth.okta(options)
        then:
        result == 2
        1 * auth.rdOutput.error({ it.contains('Error: 401: ') && it.contains(errorJson) })
        0 * auth.rdOutput.output(*_)
    }

    def "auth successful verbose"() {
        given:
        Auth auth = createAuth()
        def clientId = 'clientId'
        def clientUrl = 'clientUrl'
        def clientSecret = 'clientSecret'
        def options = new Auth.AuthOptions()
        options.clientId = clientId
        options.clientUrl = clientUrl
        options.clientSecret = clientSecret.toCharArray()
        options.scope = ['testScope1']
        options.verbose = true

        def tokenResult = new Auth.OktaToken()
        tokenResult.access_token = 'fakeAccesstoken'
        tokenResult.token_type = 'bearer'
        tokenResult.expires_in = 3600
        tokenResult.scope = 'testScope1'

        auth.oktaApiProvider = Mock(Auth.OktaApiProvider) {
            1 * get(options, _) >> Mock(Auth.OktaApi) {
                1 * token('client_credentials', 'testScope1') >> Calls.response(tokenResult)
                0 * _(*_)
            }
        }
        when:
        def result = auth.okta(options)
        then:
        result == 0
        0 * auth.rdOutput.error(*_)
        1 * auth.rdOutput.output(tokenResult)
    }

    def "secret not set no interactive prompt error"() {
        given:
        Auth auth = createAuth()
        def clientId = 'clientId'
        def clientUrl = 'clientUrl'
        def options = new Auth.AuthOptions()
        options.clientId = clientId
        options.clientUrl = clientUrl
        auth.interactive = Mock(Executions.Interactive) {
            1 * isEnabled() >> false
        }
        when:
        def result = auth.okta(options)
        then:
        result == 2
        1 * auth.rdOutput.error("No user interaction available. Use --clientSecret or --clientSecretEnv to specify client secret")
    }

    private Auth createAuth() {
        def auth = new Auth()
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def api = Mock(RundeckApi)
        def client = new Client(api, retrofit, null, null, 18, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }

        def rdtool = new MockRdTool(client: client, rdApp: hasclient)
        auth.rdTool = rdtool
        def out = Mock(CommandOutput)
        auth.rdOutput = out
        return auth
    }
}
