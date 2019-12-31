package org.rundeck.client.tool.commands.system

import okhttp3.ResponseBody
import org.rundeck.client.api.RequestFailed
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ExecutionModeLaterResponse
import org.rundeck.client.tool.Main
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import org.rundeck.toolbelt.CommandOutput
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification

class ModeSpec extends Specification {

    def "test active later"(){
        given:

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 34, true, new Main.OutputLogger(out))

        def appConfig = Mock(RdClientConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }

        def opts = Mock(Mode.ModeActiveLater) {
            getTimeValue() >> '30m'
        }

        Mode mode = new Mode(hasclient)

        when:
        def result = mode.activeLater(opts, out)

        then:
        1 * api.executionModeEnableLater(_) >>
                Calls.response(
                        new ExecutionModeLaterResponse(saved: saved, msg: 'Execution Saved')
                )

        infoCalls * out.info("Next Execution Mode will be active")
        warmCalls * out.warning("Next Execution Mode wasn't saved")
        result == saved

        where:
        saved   | infoCalls | warmCalls
        true    | 1         | 0
        false   | 0         | 1
    }

    def "test active failed"(){
        given:

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 34, true, new Main.OutputLogger(out))

        def appConfig = Mock(RdClientConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }

        def opts = Mock(Mode.ModeActiveLater) {
            getTimeValue() >> '30m'
        }

        Mode mode = new Mode(hasclient)

        when:
        def result = mode.activeLater(opts, out)

        then:
        RequestFailed e = thrown()
        1 * api.executionModeEnableLater(_) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(
                                Client.MEDIA_TYPE_JSON,
                                '{"saved":false,"msg":"Error saving execution mode"}'
                        )
                        )
                )

        0 * out.info("Next Execution Mode will be active")
        1 * out.error("Error saving execution mode")

    }

    def "test disable later"(){
        given:

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 34, true, new Main.OutputLogger(out))

        def appConfig = Mock(RdClientConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }

        def opts = Mock(Mode.ModeActiveLater) {
            getTimeValue() >> '30m'
        }

        Mode mode = new Mode(hasclient)

        when:
        def  result = mode.disableLater(opts, out)

        then:
        1 * api.executionModeDisableLater(_) >>
                Calls.response(
                        new ExecutionModeLaterResponse(saved: saved, msg: 'Execution Saved')
                )

        infoCalls * out.info("Next Execution Mode will be disable")
        warmCalls * out.warning("Next Execution Mode wasn't saved")

        result == saved

        where:
        saved   | infoCalls | warmCalls
        true    | 1         | 0
        false   | 0         | 1
    }

    def "test diable failed"(){
        given:

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 34, true, new Main.OutputLogger(out))

        def appConfig = Mock(RdClientConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }

        def opts = Mock(Mode.ModeActiveLater) {
            getTimeValue() >> '30m'
        }

        Mode mode = new Mode(hasclient)

        when:
        def result = mode.disableLater(opts, out)

        then:
        RequestFailed e = thrown()
        1 * api.executionModeDisableLater(_) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(
                                Client.MEDIA_TYPE_JSON,
                                '{"saved":false,"msg":"Error saving execution mode"}'
                        )
                        )
                )

        0 * out.info("Next Execution Mode will be active")
        1 * out.error("Error saving execution mode")

    }
}
