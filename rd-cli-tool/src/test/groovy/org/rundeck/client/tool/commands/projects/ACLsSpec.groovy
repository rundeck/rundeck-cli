package org.rundeck.client.tool.commands.projects


import okhttp3.ResponseBody
import org.rundeck.client.api.RequestFailed
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.commands.RdToolImpl
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.ProjectRequiredNameOptions
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

    private RdTool setupMock(RundeckApi api, int apiVersion = 18) {
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, apiVersion, true, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new MockRdTool(client: client, rdApp: rdapp)
        rdTool.appConfig = Mock(RdClientConfig)
        rdTool
    }


    def "upload validates acl"() {
        given:

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def acls = new ACLs()
        acls.rdOutput = out
        acls.rdTool = rdTool
        def fileOptions = new ACLs.ACLFileOptions()
        fileOptions.file = tempFile
        def nameOptions = new ACLs.ACLNameRequiredOptions()
        nameOptions.name = 'test.aclpolicy'
        def projectNameOptions = new ProjectRequiredNameOptions()
        projectNameOptions.project = 'aproject'

        when:
        def result = acls.update(nameOptions, fileOptions, projectNameOptions)

        then:

        RequestFailed e = thrown()
        1 * api.updateAclPolicy('aproject', 'test.aclpolicy', _) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(
                                '{"valid":false,"policies":[{"policy":"blah.aclpolicy[1]","errors":["Error parsing ' +
                                        'the policy document: Policy contains invalid keys: [blah], allowed keys: ' +
                                        '[by, id, for, context, description]"]}]}',
                                Client.MEDIA_TYPE_JSON
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
