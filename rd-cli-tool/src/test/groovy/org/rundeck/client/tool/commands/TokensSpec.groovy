package org.rundeck.client.tool.commands


import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ApiToken
import org.rundeck.client.api.model.CreateToken
import org.rundeck.client.api.model.DateInfo
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author greg
 * @since 5/3/17
 */
class TokensSpec extends Specification {
    @Unroll
    def "api create token response v18 has issue 2479? #issue2479"() {

        MockWebServer server = new MockWebServer();
        def responseV18 = '''{
  "user":"bob",
  "id":"123abc"
}'''
        def responseV18_issue2479 = '''{
  "user":"bob",
  "token":"123abc"
}'''

        server.enqueue(
                new MockResponse().
                        setBody(
                                issue2479 ?
                                        responseV18_issue2479 :
                                        responseV18
                        ).
                        addHeader('content-type', 'application/json')
        );
        server.start()

        def retrofit = new Retrofit.Builder().baseUrl(server.url('/api/18/')).
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        def api = retrofit.create(RundeckApi)

        when:
        def body = api.createToken("bob").execute().body()

        then:
        RecordedRequest request1 = server.takeRequest()
        request1.path == '/api/18/tokens/bob'
        request1.method == 'POST'

        body.id == expectid
        body.token == expecttoken
        body.idOrToken == '123abc'
        body.user == 'bob'
        server.shutdown()

        where:
        // https://github.com/rundeck/rundeck/issues/2479
        issue2479 | expecttoken | expectid
        false     | null        | '123abc'
        true      | '123abc'    | null
    }


    def "api create token response v19"() {

        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody('''{
  "user": "bob",
  "token": "123abc",
  "id": "c13de457-c429-4476-9acd-e1c89e3c2928",
  "creator": "user3",
  "expiration": "2017-03-24T21:18:55Z",
  "roles": [
    "a","b"
  ],
  "expired": true
}'''
        ).addHeader('content-type', 'application/json')
        );
        server.start()

        def retrofit = new Retrofit.Builder().baseUrl(server.url('/api/19/')).
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        def api = retrofit.create(RundeckApi)
        CreateToken create = new CreateToken("bob", ["a", "b"], "123m")

        when:
        def body = api.createToken(create).execute().body()

        then:
        RecordedRequest request1 = server.takeRequest()
        request1.path == '/api/19/tokens'
        request1.method == 'POST'

        body.token == '123abc'
        body.id == 'c13de457-c429-4476-9acd-e1c89e3c2928'
        body.idOrToken == 'c13de457-c429-4476-9acd-e1c89e3c2928'
        body.user == 'bob'
        body.creator == 'user3'
        body.expiration != null
        body.expiration.date == '2017-03-24T21:18:55Z'
        body.roles == ['a', 'b']
        body.expired
        server.shutdown()
    }

    @Unroll
    def "create token api v18 has issue 2479? #issue2479"() {

        def api = Mock(RundeckApi)

        def opts = Mock(Tokens.CreateOptions) {
            getUser() >> "bob"
            isUser() >> true
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Tokens tokens = new Tokens(hasclient)
        def out = Mock(CommandOutput)
        // https://github.com/rundeck/rundeck/issues/2479
        def resultToken = issue2479 ?
                new ApiToken(user: 'bob', token: '123abc') :
                new ApiToken(user: 'bob', id: '123abc')
        when:
        def result = tokens.create(opts, out)

        then:
        1 * api.createToken('bob') >> Calls.response(resultToken)
        0 * api._(*_)
        1 * out.output('123abc')

        where:
        issue2479 | _
        false     | _
        true      | _
    }

    @Unroll
    def "create token api v19"() {

        def api = Mock(RundeckApi)

        def opts = Mock(Tokens.CreateOptions) {
            getUser() >> "bob"
            isUser() >> true
            getRoles() >> ['a', 'b']
            isRoles() >> true
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 19, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Tokens tokens = new Tokens(hasclient)
        def out = Mock(CommandOutput)
        // https://github.com/rundeck/rundeck/issues/2479
        def resultToken = new ApiToken(
                user: 'bob',
                token: '123abc',
                id: 'c13de457-c429-4476-9acd-e1c89e3c2928',
                roles: ['a', 'b'],
                expired: false,
                expiration: new DateInfo('2017-03-24T21:18:55Z'),
                creator: 'user3'
        )
        when:
        def result = tokens.create(opts, out)

        then:
        1 * api.createToken({
            it instanceof CreateToken && it.roles == ['a', 'b'] && it.user == 'bob' && it.duration == null
        }
        ) >> Calls.response(resultToken)
        0 * api._(*_)
        1 * out.output('123abc')

    }

    @Unroll
    def "create token outputformat"() {

        def api = Mock(RundeckApi)

        def opts = Mock(Tokens.CreateOptions) {
            getUser() >> "bob"
            isUser() >> true
            getRoles() >> ['a', 'b']
            isRoles() >> true
            isOutputFormat() >> true
            getOutputFormat() >> format
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 19, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Tokens tokens = new Tokens(hasclient)
        def out = Mock(CommandOutput)
        // https://github.com/rundeck/rundeck/issues/2479
        def resultToken = new ApiToken(
                user: 'bob',
                token: '123abc',
                id: 'c13de457-c429-4476-9acd-e1c89e3c2928',
                roles: ['a', 'b'],
                expired: false,
                expiration: new DateInfo('2017-03-24T21:18:55Z'),
                creator: 'user3'
        )
        when:
        def result = tokens.create(opts, out)

        then:
        1 * api.createToken(
                {
                    it instanceof CreateToken && it.roles == ['a', 'b'] && it.user == 'bob' && it.duration == null
                }
        ) >> Calls.response(resultToken)
        0 * api._(*_)
        1 * out.output(expected)

        where:
        format        | expected
        "%token"      | '123abc'
        "%id"         | 'c13de457-c429-4476-9acd-e1c89e3c2928'
        "%creator"    | 'user3'
        "%user"       | 'bob'
        "%expired"    | 'false'
        "%expiration" | '2017-03-24T21:18:55Z'
        "%roles"      | '[a, b]'
        "%id:%token"  | 'c13de457-c429-4476-9acd-e1c89e3c2928:123abc'
    }
}
