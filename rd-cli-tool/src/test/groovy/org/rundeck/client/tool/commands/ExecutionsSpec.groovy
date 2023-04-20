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

import org.rundeck.client.api.model.AbortResult
import org.rundeck.client.api.model.BulkExecutionDeleteResponse
import org.rundeck.client.api.model.Execution
import org.rundeck.client.api.model.ExecutionList
import org.rundeck.client.api.model.JobItem
import org.rundeck.client.api.model.Paging
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.ExecutionOutputFormatOption
import org.rundeck.client.tool.options.PagingResultOptions
import org.rundeck.client.tool.options.ProjectNameOptions
import org.rundeck.client.util.RdClientConfig

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ExecOutput
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 12/5/16
 */
class ExecutionsSpec extends Specification {
    def "deletebulk with 0 query results result with require option"() {

        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Executions command = new Executions()
        command.rdTool = rdTool
        command.rdOutput = out

        def options = new Executions.BulkDeleteCmd()
        options.with {
            it.project = 'aproject'
            it.require = reqd
        }

        when:
        def result = command.deletebulk(options, new PagingResultOptions(), new ExecutionOutputFormatOption())
        then:
        1 * api.listExecutions('aproject', [max: '20', offset: '0'], null, null, null, null) >> Calls.response(
                new ExecutionList(paging: new Paging(offset: 0, max: 20, total: 0, count: 0), executions: [])
        )
        result == expect

        where:
        reqd  | expect
        true  | 2
        false | 0
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

    def "output format allows job.* params"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Executions command = new Executions()
        command.rdTool = rdTool
        command.rdOutput = out


        def options = new ExecutionOutputFormatOption()
        options.outputFormat = outFormat

        def jobmap = [
                id             : 'jobid',
                name           : 'jobname',
                group          : 'agroup',
                project        : 'aproject',
                description    : 'blah',
                href           : 'http://href',
                permalink      : 'http://permalink',
                averageDuration: 123l
        ]
        when:
        command.list(options, new PagingResultOptions(), new ProjectNameOptions(project: 'aproject'))

        then:
        1 * api.runningExecutions('aproject', 0, 20) >> Calls.response(
                new ExecutionList(
                        paging: new Paging(count: 1, total: 1, offset: 0, max: 20),
                        executions: [
                                new Execution(id: '1', href: 'http://no', description: '', job: new JobItem(jobmap))
                        ]
                )
        )
        1 * out.output(result)

        where:
        outFormat              | result
        '%job.id'              | 'jobid'
        '%job.name'            | 'jobname'
        '%job.group'           | 'agroup'
        '%job.project'         | 'aproject'
        '%job.description'     | 'blah'
        '%job.href'            | 'http://href'
        '%job.permalink'       | 'http://permalink'
        '%job.averageDuration' | '123'

    }
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
        def client = new Client(api, retrofit, null, null, 18, true, null)

        ExecOutput execOutput = new ExecOutput()
        execOutput.execState = initState
        execOutput.offset = 123
        execOutput.lastModified = 01L
        execOutput.entries = []
        execOutput.execCompleted = initExecCompleted
        execOutput.completed = initCompleted

        when:

        boolean result = Executions.followOutput(
                client,
                execOutput,
                progress,
                quiet,
                id,
                max,
                Mock(CommandOutput),
                { -> true },
                { -> true }
        )

        then:
        1 * api.getOutput(id, 123, 01L, max, true) >> Calls.response(execOutputFinal)
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

    def "parse execution"() {
        given:
        MockWebServer server = new MockWebServer()
        server.enqueue(new MockResponse().setBody('''{
  "id": 5418,
  "href": "http://ecto1.local:4440/api/19/execution/5418",
  "permalink": "http://ecto1.local:4440/project/adubs/execution/show/5418",
  "status": "succeeded",
  "project": "adubs",
  "user": "admin",
  "date-started": {
    "unixtime": 1492043359634,
    "date": "2017-04-13T00:29:19Z"
  },
  "date-ended": {
    "unixtime": 1492043360117,
    "date": "2017-04-13T00:29:20Z"
  },
  "job": {
    "id": "58d4de5d-5aac-438b-9b0f-252b46c9d117",
    "averageDuration": 926,
    "name": "asdf",
    "group": "",
    "project": "adubs",
    "description": "fff",
    "href": "http://ecto1.local:4440/api/19/job/58d4de5d-5aac-438b-9b0f-252b46c9d117",
    "permalink": "http://ecto1.local:4440/project/adubs/job/show/58d4de5d-5aac-438b-9b0f-252b46c9d117"
  },
  "description": "echo blah blah",
  "argstring": null,
  "serverUUID": "3425B691-7319-4EEE-8425-F053C628B4BA",
  "successfulNodes": [
    "ecto1.local"
  ]
}'''
        ).addHeader('content-type', 'application/json')
        )
        server.start()

        def retrofit = new Retrofit.Builder().baseUrl(server.url('/api/19/')).
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        def api = retrofit.create(RundeckApi)

        when:
        def body = api.getExecution('123').execute().body()

        then:
        RecordedRequest request1 = server.takeRequest()
        request1.path == '/api/19/execution/123'

        body.dateStarted != null
        body.dateStarted.date == '2017-04-13T00:29:19Z'
        server.shutdown()

    }

    def "parse compacted log"() {
        given:
        MockWebServer server = new MockWebServer()
        server.enqueue(new MockResponse().setBody('''{
  "id": 5418,
  "compacted":true,
  "compactedAttr":"log",
  "entries": [
    {"log":"test1","time":"13:02","level":"INFO","user":"bob","node":"node1","stepctx":"1"},
    "test2",
    {"log":"test3","level":"DEBUG","node":"node2", "stepctx": "2"}
  ],
  "serverUUID": "3425B691-7319-4EEE-8425-F053C628B4BA"
}'''
        ).addHeader('content-type', 'application/json')
        )
        server.start()

        def retrofit = new Retrofit.Builder().baseUrl(server.url('/api/19/')).
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        def api = retrofit.create(RundeckApi)

        when:
        def body = api.getOutput('123', 0, 0, 200, true).execute().body()

        then:
        RecordedRequest request1 = server.takeRequest()
        request1.path == '/api/19/execution/123/output?offset=0&lastmod=0&maxlines=200&compacted=true'
        body.entries != null
        body.entries.size() == 3
        body.entries[0].toMap() == [
                log: 'test1', time: '13:02', level: 'INFO', user: 'bob', node: 'node1', command: null, stepctx: "1"
        ]
        body.entries[1].toMap() == [
                log: 'test2', time: null, level: null, user: null, node: null, command: null, stepctx: null]
        body.entries[2].toMap() == [
                log: 'test3', time: null, level: 'DEBUG', user: null, node: 'node2', command: null, stepctx: "2"]

        def decomp = body.decompactEntries()
        decomp[0].toMap() == [
                log: 'test1', time: '13:02', level: 'INFO', user: 'bob', node: 'node1', command: null, stepctx: "1"
        ]
        decomp[1].toMap() == [log: 'test2', time: '13:02', level: 'INFO', user: 'bob', node: 'node1', command: null, stepctx: "1"]
        decomp[2].toMap() == [log: 'test3', time: '13:02', level: 'DEBUG', user: 'bob', node: 'node2', command: null, stepctx: "2"]


        server.shutdown()

    }

    def "executions query noninteractive --autopage behavior"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Executions command = new Executions()
        command.rdTool = rdTool
        command.rdOutput = out

        def options = new Executions.QueryCmd()
        options.project='aproject'
        options.nonInteractive=true
        options.autoLoadPages=autopage


        when:
        command.query(options, new PagingResultOptions(max:1),new ExecutionOutputFormatOption())

        then:
        1 * api.listExecutions('aproject', [max: '1', offset: '0'], null, null, null, null) >> Calls.response(
                new ExecutionList(
                        paging: new Paging(offset: 0, max: 1, total: 2, count: 1),
                        executions: [
                                new Execution(
                                        id: '1',
                                        description: ''
                                )
                        ]

                )
        )
        (page2 ? 1 : 0) * api.listExecutions('aproject', [max: '1', offset: '1'], null, null, null, null) >>
        Calls.response(
                new ExecutionList(
                        paging: new Paging(offset: 1, max: 1, total: 2, count: 1),
                        executions: [
                                new Execution(
                                        id: '2',
                                        description: ''
                                )
                        ]

                )
        )
        0 * api._(*_)

        where:
        autopage | page2
        true     | true
        false    | false
    }

    def "kill"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Executions command = new Executions()
        command.rdTool = rdTool
        command.rdOutput = out

        def options = new Executions.KillOptions()
        options.id='1'


        when:
        def result = command.kill(options)

        then:
        1 * api.abortExecution('1', false) >> Calls.response(
                new AbortResult(
                        abort: new AbortResult.Reason(status: status, reason: 'success'),
                        execution: new Execution(id: '1', description: '')
                )
        )

        0 * api._(*_)
        result==expected
        where:
        status    | expected

        'failed'  | 1
        'success' | 0
    }

    def "deleteall"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Executions command = new Executions()
        command.rdTool = rdTool
        command.rdOutput = out

        def options = new Executions.DeleteAllExecCmd()
        options.id='jobid'
        options.confirm = confirm
        command.interactive = Mock(Executions.Interactive) {
            _ * isEnabled() >> ienabled
            _ * readInteractive(*_) >> iread
        }


        when:
        def result = command.deleteall(options)

        then:
        (doesapi?1:0) * api.deleteAllJobExecutions('jobid') >> Calls.response(
                new BulkExecutionDeleteResponse(
                        allsuccessful:allsuccess,
                        failures: allsuccess?[]:[new BulkExecutionDeleteResponse.DeleteFailure(id:'jobid',message:'amessage')]
                )
        )

        0 * api._(*_)
        result==expected
        where:
        confirm | ienabled | iread | doesapi | allsuccess || expected
        false   | false    | 'y'   | false   | false      || 2
        false   | true     | 'n'   | false   | false      || 2
        false   | true     | 'y'   | true    | false      || 1
        true    | false    | null  | true    | false      || 1
        false   | true     | 'y'   | true    | true       || 0
        true    | false    | null  | true    | true       || 0
    }
}
