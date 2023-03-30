package org.rundeck.client.tool.commands.jobs

import groovy.transform.CompileStatic
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ExecutionList
import org.rundeck.client.api.model.JobFileUploadResult
import org.rundeck.client.api.model.Paging
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.commands.Executions
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.ExecutionOutputFormatOption
import org.rundeck.client.tool.options.PagingResultOptions
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

class FilesSpec extends Specification {


    def "load"() {

        given:

        File toupload = java.nio.file.Files.createTempFile('test', 'file').toFile()
        toupload << 'content'
        toupload.deleteOnExit()
        String fileName = toupload.getName()
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Files command = new Files()
        command.rdTool = rdTool
        command.rdOutput = out

        def options = new Files.FileUploadOpts()
        options.with {
            it.id = 'jobId'
            it.option = 'optionName'
            it.file = toupload
        }

        when:
        def result = command.load(options)
        then:
        1 * api.uploadJobOptionFile('jobId', 'optionName', fileName, _) >> Calls.response(
                new JobFileUploadResult(total: 1, options: resultopts)
        )
        result == expect

        cleanup:

        toupload.delete()

        where:
        resultopts              | expect
        [:]                     | 1
        [optionName: 'afileid'] | 0

    }

    private RdTool setupMock(RundeckApi api) {
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new MockRdTool(client: client, rdApp: rdapp)
        rdTool.appConfig = Mock(RdClientConfig)
        rdTool
    }

}
