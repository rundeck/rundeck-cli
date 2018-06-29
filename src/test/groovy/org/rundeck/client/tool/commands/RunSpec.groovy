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

import org.rundeck.toolbelt.CommandOutput
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.Execution
import org.rundeck.client.api.model.JobFileUploadResult
import org.rundeck.client.api.model.JobItem
import org.rundeck.client.api.model.JobRun
import org.rundeck.client.util.RdClientConfig
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.options.RunBaseOptions
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author greg
 * @since 12/13/16
 */
class RunSpec extends Specification {
    def "run command -j queries for exact job name and group"() {

        given:
        def api = Mock(RundeckApi)

        def opts = Mock(RunBaseOptions) {
            isJob() >> true
            isProject() >> true
            getProject() >> 'ProjectName'
            getJob() >> 'a group/path/a job'
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Run run = new Run(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = run.run(opts, out)

        then:
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, null, null, null) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result

    }

    @Unroll
    def "run gets project from ENV if specified = false #isproj"() {

        given:
        def api = Mock(RundeckApi)

        def opts = Mock(RunBaseOptions) {
            isJob() >> true
            isProject() >> isproj
            getProject() >> (isproj ? "ProjectName" : null)
            getJob() >> 'a group/path/a job'
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def appConfig = Mock(RdClientConfig)
        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        Run run = new Run(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = run.run(opts, out)

        then:
        if(!isproj) {
            1 * appConfig.require('RD_PROJECT', _) >> "ProjectName"
        }
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, null, null, null) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result

        where:
        isproj | _
        true   | _
        false  | _
    }

    def "run argstring supports -opt @path and -opt@ path"() {
        given:
        def api = Mock(RundeckApi)
        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")


        def opts = Mock(RunBaseOptions) {
            isId() >> true
            getId() >> 'jobid1'
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
        def client = new Client(api, retrofit, null, null, 19, true, null)
        def appConfig = Mock(RdClientConfig)
        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        Run run = new Run(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = run.run(opts, out)

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
        result
    }

    def "run --raw argstring supports -opt@ path only"() {
        given:
        def api = Mock(RundeckApi)
        def testfile1 = File.createTempFile("upload1", "test")
        def testfile2 = File.createTempFile("upload2", "test")


        def opts = Mock(RunBaseOptions) {
            isId() >> true
            getId() >> 'jobid1'
            isRawOptions()>>true
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
        def client = new Client(api, retrofit, null, null, 19, true, null)
        def appConfig = Mock(RdClientConfig)
        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        Run run = new Run(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = run.run(opts, out)

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
        result
    }
}
