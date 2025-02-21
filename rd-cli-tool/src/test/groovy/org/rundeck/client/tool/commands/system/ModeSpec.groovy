package org.rundeck.client.tool.commands.system


import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ExecutionMode
import org.rundeck.client.api.model.SystemInfo
import org.rundeck.client.api.model.SystemMode
import org.rundeck.client.api.model.sysinfo.SystemStats
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
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

    def "mode info when system info has mode data"() {
        def api = Mock(RundeckApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        Mode command = new Mode()
        command.rdOutput = out
        command.rdTool = rdTool
        def opts = new Mode.ModeInfo()
        opts.testActive = testActive
        opts.testPassive = testPassive

        when: "system info has execution mode data"
        def result = command.info(opts)

        then:
        1 * api.systemInfo() >> Calls.response(new SystemInfo(system: new SystemStats((Map) statsData)))
        1 * out.output(statsData.executions.executionMode)
        (1-expected) * out.info('Execution Mode is currently:')
        (expected) * out.warning('Execution Mode is currently:')
        0 * api._(*_)
        result == expected
        where:
        statsData                                | testActive | testPassive | expected
        [executions: [executionMode: 'active']]  | true       | false       | 0
        [executions: [executionMode: 'active']]  | false      | true        | 1
        [executions: [executionMode: 'passive']] | true       | false       | 1
        [executions: [executionMode: 'passive']] | false      | true        | 0
        [executions: [executionMode: 'other']]   | true       | false       | 1
        [executions: [executionMode: 'other']]   | false      | true        | 0
    }
    def "mode info when system info has no mode data"() {
        def api = Mock(RundeckApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        Mode command = new Mode()
        command.rdOutput = out
        command.rdTool = rdTool
        def opts = new Mode.ModeInfo()
        opts.testActive = true
        opts.testPassive = false

        when: "system info has execution mode data"
        def result = command.info(opts)

        then:
        1 * api.systemInfo() >> Calls.response(new SystemInfo(system: new SystemStats((Map) statsData)))
        0 * out._(*_)
        0 * api._(*_)
        RuntimeException e = thrown()

        where:
        statsData = [:]
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
