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

import com.simplifyops.toolbelt.CommandOutput
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


        def output = Calls.response(execOutput)

        when:

        boolean result = Executions.followOutput(
                client,
                output,
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
        MockWebServer server = new MockWebServer();
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
        );
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
        MockWebServer server = new MockWebServer();
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
        );
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
}
