package org.rundeck.client.tool.commands.projects

import org.rundeck.toolbelt.CommandOutput
import okhttp3.ResponseBody
import org.rundeck.client.api.RequestFailed
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.util.RdClientConfig
import org.rundeck.client.tool.Main
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 11/16/17
 */
class ACLsSpec extends Specification {
    File tempFile = File.createTempFile('data', 'aclpolicy')

    def setup() {
        tempFile = File.createTempFile('data', 'aclpolicy')
    }

    def cleanup() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    def "upload validates acl"() {
        given:

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, new Main.OutputLogger(out))

        def appConfig = Mock(RdClientConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        def opts = Mock(ACLs.Put) {
            getProject() >> 'aproject'
            getName() >> 'test.aclpolicy'
            getFile() >> tempFile
        }
        def acls = new ACLs(hasclient)

        when:
        def result = acls.upload(opts, out)

        then:

        RequestFailed e = thrown()
        1 * api.updateAclPolicy('aproject', 'test.aclpolicy', _) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(
                                Client.MEDIA_TYPE_JSON,
                                '{"valid":false,"policies":[{"policy":"blah.aclpolicy[1]","errors":["Error parsing ' +
                                        'the policy document: Policy contains invalid keys: [blah], allowed keys: ' +
                                        '[by, id, for, context, description]"]}]}'
                        )
                        )
                )
        1 * out.error("ACL Policy Validation failed for the file: ")
        1 * out.output(tempFile.getAbsolutePath())
        1 * out.output(
                ['blah.aclpolicy[1]': ['Error parsing the policy document: Policy contains invalid keys: [blah], ' +
                                               'allowed keys: [by, id, for, context, description]']]
        )

    }
}
