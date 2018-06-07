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
import org.rundeck.client.api.model.JobItem
import org.rundeck.client.api.model.JobRun
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.options.RetryBaseOptions
import org.rundeck.client.tool.options.RunBaseOptions
import org.rundeck.client.util.Client
import org.rundeck.toolbelt.CommandOutput
import org.rundeck.toolbelt.InputError
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll


class RetrySpec extends Specification {
    def "run argstring supports -opt @path and -opt@ path"() {
        given:
        def api = Mock(RundeckApi)
        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")


        def opts = Mock(RetryBaseOptions) {
            isId() >> true
            getId() >> 'jobid1'
            getEid() >> 'eid'
            getCommandString() >> [
                    "-opt1",
                    "val1",
                    "-opt2",
                    "@$testfile1.absolutePath",
                    "-opt3@",
                    testfile2.absolutePath
            ].collect { it.toString() }
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 24, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Retry retry = new Retry(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = retry.retry(opts, out)

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
        result
    }
    def "error on api version below 24"() {
        given:
        def api = Mock(RundeckApi)
        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")


        def opts = Mock(RetryBaseOptions) {
            isId() >> true
            getId() >> 'jobid1'
            getEid() >> 'eid'
            getCommandString() >> [
                    "-opt1",
                    "val1",
                    "-opt2",
                    "@$testfile1.absolutePath",
                    "-opt3@",
                    testfile2.absolutePath
            ].collect { it.toString() }
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 23, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Retry retry = new Retry(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = retry.retry(opts, out)

        then:
        0 * api._(*_)
        InputError e = thrown()
    }
}
