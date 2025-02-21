package org.rundeck.client.tool.commands

import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.SystemInfo
import org.rundeck.client.api.model.sysinfo.SystemStats
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

class RDSystemSpec extends Specification {

    @Unroll
    def "system info data is minimal"() {

        given: "system info response is minimal"

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        RDSystem cmd = new RDSystem()
        def rdtool = new MockRdTool(client: client, rdApp: hasclient)
        cmd.rdTool = rdtool
        def out = Mock(CommandOutput)
        cmd.rdOutput = out
        when: "system info is called"
        cmd.info()
        then: "api call has correct values"
        1 * api.systemInfo() >> Calls.response(new SystemInfo(system: new SystemStats((Map) statsData)))
        1 * out.output(expected)

        0 * api._(*_)

        where:
        statsData                 | expected
        [:]                       | [:]
        [rundeck: [some: 'data']] | [rundeck: [some: 'data']]
    }
}
