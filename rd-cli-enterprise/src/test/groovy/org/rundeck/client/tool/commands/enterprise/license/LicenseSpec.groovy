package org.rundeck.client.tool.commands.enterprise.license

import groovy.transform.CompileStatic
import okhttp3.ResponseBody
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.KeyStorageItem
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.commands.enterprise.api.EnterpriseApi
import org.rundeck.client.tool.commands.enterprise.api.model.LicenseResponse
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

class LicenseSpec extends Specification {
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

    def "status"() {
        def api = Mock(EnterpriseApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        License command = new License()
        command.rdTool = rdTool
        def opts = new License.StatusOpts()


        when:
        def result = command.status(opts)

        then:
        1 * api.verifyLicense() >> Calls.response(new LicenseResponse())
        0 * api._(*_)
        result == 0
    }

    def "status remaining"() {
        def api = Mock(EnterpriseApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        License command = new License()
        command.rdTool = rdTool
        def opts = new License.StatusOpts()
        opts.remaining = optremain


        when:
        def result = command.status(opts)

        then:
        1 * api.verifyLicense() >> Calls.response(new LicenseResponse(remaining: remaining))
        0 * api._(*_)
        result == expect
        where:
        optremain | remaining | expect
        10        | 10        | 0
        10        | 9         | 1
        10        | 0         | 1
    }

    def "status expired"() {
        def api = Mock(EnterpriseApi)
        def out = Mock(CommandOutput)
        RdTool rdTool = setupMock(api, out, 41)
        License command = new License()
        command.rdTool = rdTool
        def opts = new License.StatusOpts()
        opts.status = true


        when:
        def result = command.status(opts)

        then:
        1 * api.verifyLicense() >> Calls.response(new LicenseResponse(active: active))
        0 * api._(*_)
        result == expect
        where:
        active | expect
        true   | 0
        false  | 1
    }
}
