package org.rundeck.client.tool.commands

import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.AdhocResponse
import org.rundeck.client.api.model.Execution
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import org.rundeck.toolbelt.CommandOutput
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

class AdhocSpec extends Specification {
    @Unroll
    def "run adhoc scriptFile with script interpreter"() {

        def tempFile = File.createTempFile('script', '.sh')
        def scriptUrl = new URL('http://example.com')
        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        Adhoc adhoc = new Adhoc(hasclient)
        def out = Mock(CommandOutput)

        given: "interpreter extension and quoted options"
            def options = Mock(Adhoc.AdhocOptions) {
                getProject() >> 'aproject'
                getScriptInterpreter() >> interpreter
                isScriptInterpreter() >> (interpreter != null)
                getFileExtension() >> fileExtension
                isFileExtension() >> (fileExtension != null)
                isArgsQuoted() >> argsQuoted
                getScriptFile() >> (!url ? tempFile : null)
                isScriptFile() >> (!url)
                getUrl() >> (url ? scriptUrl : null)
                isUrl() >> url
            }
        when: "we run adhoc script file"
            adhoc.adhoc(options, out)
        then: "api call has correct values"
            1 * api."${url ? 'runUrl' : 'runScript'}"(
                    'aproject',
                    (url ? scriptUrl : _),
                    0,
                    false,
                    null,
                    interpreter,
                    argsQuoted,
                    fileExtension,
                    null
            ) >> Calls.response(new AdhocResponse(message: 'ok', execution: new Execution(id: '123')))
            1 * api.getExecution('123') >> Calls.response(new Execution(id: '123', description: 'asdf'))
            0 * api._(*_)

        cleanup:
            tempFile.delete()
        where:
            interpreter  | argsQuoted | fileExtension | url
            'asdf'       | false      | null          | false
            null         | true       | null          | false
            null         | false      | '.psh'        | false
            'powershell' | true       | '.psh'        | false
            'asdf'       | false      | null          | true
            null         | true       | null          | true
            null         | false      | '.psh'        | true
            'powershell' | true       | '.psh'        | true
    }
}
