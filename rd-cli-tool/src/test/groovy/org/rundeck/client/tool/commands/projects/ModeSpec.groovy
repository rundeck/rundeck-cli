package org.rundeck.client.tool.commands.projects

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


    def "enable execution later test"(){

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
            getProject() >> 'aproject'
            getType() >> 'executions'
            getTimeValue() >> '30m'
        }

        Mode projectExecutionMode = new Mode(hasclient)

        when:

        projectExecutionMode.enableLater(opts, out)

        then:

        1 * api.projectExecutionModeEnableLater('aproject',_) >>
                Calls.response(
                        new ExecutionModeLaterResponse(saved: saved, msg: 'Execution Saved')
            )

        infoCalls * out.info("executions will be enable after 30m")
        warmCalls * out.warning("executions mode wasn't save")

        where:
        saved   | infoCalls | warmCalls
        true    | 1         | 0
        false   | 0         | 1

    }

    def "enable execution later failed"(){

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
            getProject() >> 'aproject'
            getType() >> 'executions'
            getTimeValue() >> '30m'
        }

        Mode projectExecutionMode = new Mode(hasclient)

        when:

        projectExecutionMode.enableLater(opts, out)

        then:
        RequestFailed e = thrown()

        1 * api.projectExecutionModeEnableLater('aproject',_) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(
                                Client.MEDIA_TYPE_JSON,
                                '{"saved":false,"msg":"Error saving execution mode"}'
                        )
                        )
                )

        0 * out.info("executions will be enable after 30m")
        1 * out.error("Error saving execution mode")

    }

    def "disable execution later test"(){

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
            getProject() >> 'aproject'
            getType() >> 'executions'
            getTimeValue() >> '30m'
        }

        Mode projectExecutionMode = new Mode(hasclient)

        when:

        projectExecutionMode.disableLater(opts, out)

        then:

        1 * api.projectExecutionModeDisableLater('aproject',_) >>
                Calls.response(
                        new ExecutionModeLaterResponse(saved: saved, msg: 'Execution Saved')
                )

        infoCalls * out.info("executions will be disable after 30m")
        warmCalls * out.warning("executions mode wasn't save")

        where:
        saved   | infoCalls | warmCalls
        true    | 1         | 0
        false   | 0         | 1

    }

    def "disable execution later failed"(){

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
            getProject() >> 'aproject'
            getType() >> 'executions'
            getTimeValue() >> '30m'
        }

        Mode projectExecutionMode = new Mode(hasclient)

        when:

        projectExecutionMode.disableLater(opts, out)

        then:
        RequestFailed e = thrown()

        1 * api.projectExecutionModeDisableLater('aproject',_) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(
                                Client.MEDIA_TYPE_JSON,
                                '{"saved":false,"msg":"Error saving execution mode"}'
                        )
                        )
                )

        0 * out.info("executions will be enable after 30m")
        1 * out.error("Error saving execution mode")

    }
}
