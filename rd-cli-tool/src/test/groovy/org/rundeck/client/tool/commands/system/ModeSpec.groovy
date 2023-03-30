package org.rundeck.client.tool.commands.system

import groovy.transform.CompileStatic
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ExecutionMode
import org.rundeck.client.api.model.SystemMode
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi
import org.rundeck.client.tool.commands.enterprise.api.model.EnterpriseModeResponse
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.QuietOption
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

class ModeSpec extends Specification {
    private RdTool setupMock(RundeckApi api, CommandOutput out, int apiVersion) {
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, apiVersion, true, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
            getOutput() >> out
        }
        def rdTool = new MockRdTool(client: client, rdApp: rdapp)
        rdTool.appConfig = Mock(RdClientConfig)
        rdTool
    }

    def "passive"() {
        def api = Mock(RundeckApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        Mode command = new Mode()
        command.rdOutput = out
        command.rdTool = rdTool
        def opts = new QuietOption()


        when:
        def result = command.passive(opts)

        then:
        1 * api.executionModeDisable() >> Calls.response(new SystemMode(executionMode: respstatus as ExecutionMode))
        0 * api._(*_)
        result == expected
        where:
        respstatus | expected
        'active'   | 1
        'passive'  | 0
    }

    def "active"() {
        def api = Mock(RundeckApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        Mode command = new Mode()
        command.rdOutput = out
        command.rdTool = rdTool
        def opts = new QuietOption()


        when:
        def result = command.active(opts)

        then:
        1 * api.executionModeEnable() >> Calls.response(new SystemMode(executionMode: respstatus as ExecutionMode))
        0 * api._(*_)
        result == expected
        where:
        respstatus | expected
        'active'   | 0
        'passive'  | 1
    }
}
