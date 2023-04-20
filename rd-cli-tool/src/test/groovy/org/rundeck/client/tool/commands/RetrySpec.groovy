/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
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
import org.rundeck.client.api.model.Execution
import org.rundeck.client.api.model.JobFileUploadResult
import org.rundeck.client.api.model.JobRun
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.InputError
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.FollowOptions
import org.rundeck.client.tool.options.RetryBaseOptions
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll


class RetrySpec extends Specification {

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

    def "run argstring supports -opt @path and -opt@ path"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api,24)
        def out = Mock(CommandOutput)
        Retry command = new Retry()
        command.rdTool = rdTool
        command.rdOutput = out
        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")


        command.followOptions = new FollowOptions()
        command.options = new RetryBaseOptions()
        command.options.id = 'jobid1'
        command.options.eid = 'eid'
        command.options.commandString = [
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
        1 * api.retryJob('jobid1', 'eid', { JobRun runarg ->
            runarg.options == ['opt1': 'val1', 'opt2': 'fakefileid1', 'opt3': 'fakefileid2']
        }
        ) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result==0
    }
    def "error on api version below 24"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api,23)
        def out = Mock(CommandOutput)
        Retry command = new Retry()
        command.rdTool = rdTool
        command.rdOutput = out
        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")


        command.followOptions = new FollowOptions()
        command.options = new RetryBaseOptions()
        command.options.id = 'jobid1'
        command.options.eid = 'eid'
        command.options.commandString = [
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
        0 * api._(*_)
        InputError e = thrown()
    }
}
