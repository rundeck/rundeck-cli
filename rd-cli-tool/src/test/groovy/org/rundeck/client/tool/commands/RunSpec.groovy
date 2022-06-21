/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.tool.commands

import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ExecOutput
import org.rundeck.client.api.model.Execution
import org.rundeck.client.api.model.JobFileUploadResult
import org.rundeck.client.api.model.JobItem
import org.rundeck.client.api.model.JobRun
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.RunBaseOptions
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author greg
 * @since 12/13/16
 */
class RunSpec extends Specification {

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

    def "run command -f exits #code when execution status is #status"() {

        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 17)
        def out = Mock(CommandOutput)
        Run command = new Run()
        command.rdTool = rdTool
        command.rdOutput = out

        command.options.project = 'ProjectName'
        command.options.job = 'a group/path/a job'
        command.followOptions.follow = true
        def output = new ExecOutput()
        output.execCompleted = true
        output.completed = true
        output.entries = []
        output.execState = status

        when:
        def result = command.call()

        then:
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, null, null, null) >> Calls.response(new Execution(id: 123, description: ''))
        1 * api.getOutput('123', 0, 0, 500, true) >> Calls.response(output)
        0 * api._(*_)
        result == code
        where:
        status      | code
        'succeeded' | 0
        'failed'    | 2
        'aborted'   | 2
        'other'     | 2
    }

    def "run command -j queries for exact job name and group"() {

        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 17)
        def out = Mock(CommandOutput)
        Run command = new Run()
        command.rdTool = rdTool
        command.rdOutput = out

        command.options.project = 'ProjectName'
        command.options.job = 'a group/path/a job'

        when:
        def result = command.call()

        then:
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, null, null, null) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result == 0

    }

    def "run command loglevel debug"() {

        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 17)
        def out = Mock(CommandOutput)
        Run command = new Run()
        command.rdTool = rdTool
        command.rdOutput = out

        command.options.project = 'ProjectName'
        command.options.job = 'a group/path/a job'
        command.options.loglevel = inlevel

        when:
        def result = command.call()

        then:
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, seenlevel, null, null) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result == 0
        where:
        inlevel                         | seenlevel
        RunBaseOptions.Loglevel.debug   | 'DEBUG'
        RunBaseOptions.Loglevel.verbose | 'VERBOSE'
        RunBaseOptions.Loglevel.info    | 'INFO'
        RunBaseOptions.Loglevel.warning | 'WARNING'
        RunBaseOptions.Loglevel.error   | 'ERROR'

    }
    def "run command --outformat applies to exec data"() {

        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 17)
        def out = Mock(CommandOutput)
        Run command = new Run()
        command.rdTool = rdTool
        command.rdOutput = out

        command.options.project = 'ProjectName'
        command.options.job = 'a group/path/a job'
        command.options.loglevel = RunBaseOptions.Loglevel.debug
        command.outputFormatOption.outputFormat='%id'


        when:
        def result = command.call()

        then:
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, 'DEBUG', null, null) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        1 * out.output("123")
        result == 0

    }
    @Unroll
    def "run command --verbose outputs exec data map"() {

        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 17)
        def out = Mock(CommandOutput)
        Run command = new Run()
        command.rdTool = rdTool
        command.rdOutput = out

        command.options.project = 'ProjectName'
        command.options.job = 'a group/path/a job'
        command.options.loglevel = RunBaseOptions.Loglevel.debug
        command.outputFormatOption.verbose=true

        def job = new JobItem(id: 'fakeid')

        when:
        def result = command.call()

        then:
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([job])
        1 * api.runJob('fakeid', null, 'DEBUG', null, null) >> Calls.response(new Execution(id: 123, description: '',job: hasJob?job:null))
        0 * api._(*_)
        1 * out.output({
            it == [
                [
                    'failedNodes'    : null,
                    'description'    : '',
                    'project'        : null,
                    'successfulNodes': null,
                    'argstring'      : null,
                    'serverUUID'     : null,
                    'dateStarted'    : null,
                    'dateEnded'      : null,
                    'id'             : '123',
                    'href'           : null,
                    'permalink'      : null,
                    'user'           : null,
                    'status'         : null,
                    'adhoc'          : adhoc,
                ] + (
                    hasJob ? [
                        job: [
                            'id'         : 'fakeid',
                            'name'       : null,
                            'project'    : null,
                            'href'       : null,
                            'permalink'  : null,
                            'description': null
                        ]
                    ] :
                    [:]
                )
            ]
        })
        result == 0
        where:
            hasJob | adhoc
            true | 'false'
            false | 'true'

    }


    def "run argstring supports -opt @path and -opt@ path"() {
        given:

        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")

        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 19)
        def out = Mock(CommandOutput)
        Run command = new Run()
        command.rdTool = rdTool
        command.rdOutput = out

        command.options.project = 'ProjectName'
        command.options.id = 'jobid1'
        command.options.commandString=[
                "-opt1",
                "val1",
                "-opt2",
                "@$testfile1.absolutePath",
                "-opt3@",
                testfile2.absolutePath
        ].collect { it.toString() }

        when:
        def result = command.call()

        then:
        1 * api.uploadJobOptionFile('jobid1', 'opt2', testfile1.name, _) >> Calls.response(
                new JobFileUploadResult(total: 1, options: ['opt2': 'fakefileid1'])
        )
        1 * api.uploadJobOptionFile('jobid1', 'opt3', testfile2.name, _) >> Calls.response(
                new JobFileUploadResult(total: 1, options: ['opt3': 'fakefileid2'])
        )
        1 * api.runJob('jobid1', { JobRun runarg ->
            runarg.options == ['opt1': 'val1', 'opt2': 'fakefileid1', 'opt3': 'fakefileid2']
        }
        ) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result == 0
    }

    def "run --raw argstring supports -opt@ path only"() {
        given:

        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 19)
        def out = Mock(CommandOutput)
        Run command = new Run()
        command.rdTool = rdTool
        command.rdOutput = out

        command.options.project = 'ProjectName'
        command.options.id = 'jobid1'
        command.options.rawOptions=true
        command.options.commandString=[
                "-opt1",
                "val1",
                "-opt2",
                "@$testfile1.absolutePath",
                "-opt3@",
                testfile2.absolutePath
        ].collect { it.toString() }

        when:
        def result = command.call()

        then:
        0 * api.uploadJobOptionFile('jobid1', 'opt2', testfile1.name, _) >> Calls.response(
                new JobFileUploadResult(total: 1, options: ['opt2': 'fakefileid1'])
        )
        1 * api.uploadJobOptionFile('jobid1', 'opt3', testfile2.name, _) >> Calls.response(
                new JobFileUploadResult(total: 1, options: ['opt3': 'fakefileid2'])
        )
        1 * api.runJob('jobid1', { JobRun runarg ->
            runarg.options == ['opt1': 'val1', 'opt2': "@$testfile1.absolutePath", 'opt3': 'fakefileid2']
        }
        ) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result == 0
    }
}
