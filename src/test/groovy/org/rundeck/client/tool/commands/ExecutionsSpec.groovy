package org.rundeck.client.tool.commands

import com.simplifyops.toolbelt.CommandOutput
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ExecOutput
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 12/5/16
 */
class ExecutionsSpec extends Specification {
    def "followOutput repeats until execution is not running"() {
        given:
        def max = 500
        def id = '123'
        def quiet = false
        def progress = false

        ExecOutput execOutputFinal = new ExecOutput()
        execOutputFinal.execState = finalState
        execOutputFinal.execCompleted = true
        execOutputFinal.completed = true
        execOutputFinal.entries = []

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 18)

        ExecOutput execOutput = new ExecOutput()
        execOutput.execState = initState
        execOutput.offset = 123
        execOutput.lastModified = 01L
        execOutput.entries = []
        execOutput.execCompleted = initExecCompleted
        execOutput.completed = initCompleted


        def output = Calls.response(execOutput)

        when:

        boolean result = Executions.followOutput(client, output, progress, quiet, id, max, Mock(CommandOutput)) {
            -> true
        }

        then:
        1 * api.getOutput(id, 123, 01L, max) >> Calls.response(execOutputFinal)
        result == exit

        where:
        initState   | initExecCompleted | initCompleted | finalState  | exit
        'running'   | false             | false         | 'succeeded' | true
        'running'   | true              | false         | 'succeeded' | true
        'running'   | false             | true          | 'succeeded' | true
        'running'   | false             | false         | 'failed'    | false
        'running'   | true              | false         | 'failed'    | false
        'running'   | false             | true          | 'failed'    | false
        'scheduled' | false             | false         | 'succeeded' | true
        'scheduled' | true              | false         | 'succeeded' | true
        'scheduled' | false             | true          | 'succeeded' | true
        'scheduled' | false             | false         | 'failed'    | false
        'scheduled' | true              | false         | 'failed'    | false
        'scheduled' | false             | true          | 'failed'    | false

    }
}
