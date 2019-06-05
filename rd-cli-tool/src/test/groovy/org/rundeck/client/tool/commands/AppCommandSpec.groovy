package org.rundeck.client.tool.commands

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.rundeck.client.api.RequestFailed
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.client.util.ServiceClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 10/19/17
 */
class AppCommandSpec extends Specification {
    def "check error with api version downgrade"() {
        given:

        def retrofit = new Retrofit.Builder().baseUrl('http://test').
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        ServiceClient<RundeckApi> client = new Client(
                Mock(RundeckApi),
                retrofit,
                "appBaseUrl",
                "apiBaseUrl",
                21,
                true,
                null
        )
        ServiceClient<RundeckApi> client2 = Mock(ServiceClient)

        def app = Mock(RdApp) {
            getClient() >> client
        }

        def cmd = new AppCommand(app) {

        }


        when:
        def result = cmd.apiCall {
            Calls.response(
                    Response.error(
                            400,
                            ResponseBody.create(
                                    MediaType.parse('application/json'),
                                    '{"error":true,"apiversion":20,"errorCode":"api.error.api-version.unsupported",' +
                                            '"message":"Unsupported API Version \\"21\\". API Request: ' +
                                            '/api/21/projects. Reason: Current version: 20"}'
                            )
                    )
            )
        }

        then:
        1 * app.getClient(20) >> client2
        1 * client2.apiCall(_) >> 'test result'
        result == 'test result'
    }

    def "check error without api version downgrade"() {
        given:

        def retrofit = new Retrofit.Builder().baseUrl('http://test').
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        ServiceClient<RundeckApi> client = new Client(
                Mock(RundeckApi),
                retrofit,
                "appBaseUrl",
                "apiBaseUrl",
                21,
                false,
                Mock(Client.Logger)
        )
        ServiceClient<RundeckApi> client2 = Mock(ServiceClient)

        def app = Mock(RdApp) {
            getClient() >> client
        }

        def cmd = new AppCommand(app) {

        }


        when:
        def result = cmd.apiCall {
            Calls.response(
                    Response.error(
                            400,
                            ResponseBody.create(
                                    MediaType.parse('application/json'),
                                    '{"error":true,"apiversion":20,"errorCode":"api.error.api-version.unsupported",' +
                                            '"message":"Unsupported API Version \\"21\\". API Request: ' +
                                            '/api/21/projects. Reason: Current version: 20"}'
                            )
                    )
            )
        }

        then:
        RequestFailed e = thrown()
        e.message=='Request failed: 400 null'
    }
}
