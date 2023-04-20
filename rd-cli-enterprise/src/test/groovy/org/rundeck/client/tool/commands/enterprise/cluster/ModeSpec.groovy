package org.rundeck.client.tool.commands.enterprise.cluster

import groovy.transform.CompileStatic
import org.rundeck.client.api.model.ExecutionMode
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi
import org.rundeck.client.tool.commands.enterprise.api.model.EnterpriseModeResponse
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseResponse
import org.rundeck.client.tool.commands.enterprise.license.License
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

class ModeSpec extends Specification {
    private RdTool setupMock(EnterpriseApi api, CommandOutput out, int apiVersion) {
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, apiVersion, true, null)
        def rdapp = Mock(RdApp) {
            getClient(EnterpriseApi) >> client
            getAppConfig() >> Mock(RdClientConfig)
            getOutput() >> out
        }
        def rdTool = new MockRdTool(client: client, rdApp: rdapp)
        rdTool.appConfig = Mock(RdClientConfig)
        rdTool
    }

    def "active"() {
        def api = Mock(EnterpriseApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        Mode command = new Mode()
        command.rdTool = rdTool
        def opts = new Mode.Options()
        opts.uuid = 'uuid'


        when:
        def result = command.active(opts)

        then:
        1 * api.executionModeEnable('uuid') >> Calls.response(new EnterpriseModeResponse(executionMode: respstatus as ExecutionMode))
        0 * api._(*_)
        result == expected
        where:
        respstatus | expected
        'active'   | 0
        'passive'  | 1
    }
    def "passive"() {
        def api = Mock(EnterpriseApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        Mode command = new Mode()
        command.rdTool = rdTool
        def opts = new Mode.Options()
        opts.uuid = 'uuid'


        when:
        def result = command.passive(opts)

        then:
        1 * api.executionModeDisable('uuid') >> Calls.response(new EnterpriseModeResponse(executionMode: respstatus as ExecutionMode))
        0 * api._(*_)
        result == expected
        where:
        respstatus | expected
        'active'   | 1
        'passive'  | 0
    }
}
