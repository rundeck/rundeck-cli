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
import org.rundeck.toolbelt.InputError
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.DeleteJobsResult
import org.rundeck.client.api.model.ImportResult
import org.rundeck.client.api.model.JobItem
import org.rundeck.client.api.model.JobLoadItem
import org.rundeck.client.api.model.ScheduledJobItem
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author greg
 * @since 12/13/16
 */
class JobsSpec extends Specification {
    File tempFile = File.createTempFile('data', 'out')

    def setup() {
        tempFile = File.createTempFile('data', 'out')
    }

    def cleanup() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    def "job list with input parameters"() {
        given:
        def api = Mock(RundeckApi)

        def opts = Mock(Jobs.ListOpts) {
            isJob() >> true
            isProject() >> true
            getProject() >> 'ProjectName'
            getJobExact() >> jobexact
            getGroupExact() >> groupexact
            getJob() >> job
            getGroup() >> group
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        jobs.list(opts, out)

        then:
        1 * api.listJobs('ProjectName', job, group, jobexact, groupexact) >>
                Calls.response([new JobItem(id: 'fakeid')])
        0 * api._(*_)

        where:
        job  | group | jobexact | groupexact
        'a'  | 'b/c' | null     | null
        null | null  | 'a'      | 'b/c'
    }

    def "job list write to file with input parameters"() {
        given:
        def api = Mock(RundeckApi)
        def opts = Mock(Jobs.ListOpts) {
            isJob() >> true
            isProject() >> true
            getProject() >> 'ProjectName'
            getJobExact() >> jobexact
            getGroupExact() >> groupexact
            getJob() >> job
            getGroup() >> group
            isFile() >> true
            getFormat() >> 'xml'
            getFile() >> tempFile
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        jobs.list(opts, out)

        then:
        1 * api.exportJobs('ProjectName', job, group, jobexact, groupexact, 'xml') >>
                Calls.response(ResponseBody.create(MediaType.parse('application/xml'), 'abc'))
        0 * api._(*_)
        tempFile.exists()
        tempFile.text == 'abc'

        where:
        job  | group | jobexact | groupexact
        'a'  | 'b/c' | null     | null
        null | null  | 'a'      | 'b/c'
    }

    @Unroll
    def "jobs #action behavior"() {
        given:
        def api = Mock(RundeckApi)
        def deets = [
                enable    : [
                        opt : Jobs.EnableOpts,
                        call: "jobExecutionEnable"
                ],

                disable   : [
                        opt : Jobs.DisableOpts,
                        call: "jobExecutionDisable"
                ],

                reschedule: [
                        opt : Jobs.EnableSchedOpts,
                        call: "jobScheduleEnable"
                ],

                unschedule: [
                        opt : Jobs.DisableSchedOpts,
                        call: "jobScheduleDisable"
                ]
        ]
        Class optClass = deets[action].opt
        def apiCall = deets[action].call
        def opts = Mock(optClass) {
            isId() >> true
            getId() >> '123'
            getProject() >> 'testProj'
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        jobs."$action"(opts, out)

        then:
        1 * api."$apiCall"('123') >> Calls.response(new Simple(success: true))
        0 * api._(*_)

        where:
        action       | _
        'enable'     | _
        'disable'    | _
        'unschedule' | _
        'reschedule' | _
    }


    @Unroll
    def "job purge with job #job group #group jobexact #jobexact groupexact #groupexact"() {
        given:
        def api = Mock(RundeckApi)

        def opts = Mock(Jobs.Purge) {
            isJob() >> (job != null)
            isGroup() >> (group != null)
            isJobExact() >> (jobexact != null)
            isGroupExact() >> (groupexact != null)
            isProject() >> true
            getProject() >> 'ProjectName'
            getJobExact() >> jobexact
            getGroupExact() >> groupexact
            getJob() >> job
            getGroup() >> group
            isConfirm() >> true
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = jobs.purge(opts, out)

        then:
        1 * api.listJobs('ProjectName', job, group, jobexact, groupexact) >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.deleteJobs(['fakeid']) >> Calls.response(new DeleteJobsResult(allsuccessful: true))
        0 * api._(*_)
        result

        where:
        job  | group | jobexact | groupexact
        'a'  | null  | null     | null
        'a'  | 'b/c' | null     | null
        null | 'b/c' | null     | null
        null | null  | 'a'      | null
        null | null  | 'a'      | 'b/c'
        null | null  | null     | 'b/c'
    }

    def "job purge invalid input"() {
        given:
        def api = Mock(RundeckApi)

        def opts = Mock(Jobs.Purge) {
            isJob() >> false
            isGroup() >> false
            isJobExact() >> false
            isGroupExact() >> false
            isProject() >> true
            getProject() >> 'ProjectName'
            isConfirm() >> true
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        jobs.purge(opts, out)

        then:
        InputError e = thrown()
        e.message == 'must specify -i, or -j/-g/-J/-G to specify jobs to delete.'

    }

    def "jobs info outformat"() {
        given:

        def api = Mock(RundeckApi)
        def opts = Mock(Jobs.InfoOpts) {
            getId() >> "123"
            getOutputFormat() >> outFormat
            isOutputFormat() >> true
        }

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)

        when:
        jobs.info(opts, out)

        then:
        1 * api.getJobInfo('123') >> Calls.response(new ScheduledJobItem(id: '123', href: 'monkey'))
        1 * out.output([result])


        where:
        outFormat   | result
        '%id %href' | '123 monkey'
    }

    def "job load with errors produces output"() {
        given:
        def api = Mock(RundeckApi)
        def opts = Mock(Jobs.Load) {
            isProject() >> true
            getProject() >> 'ProjectName'
            getFormat() >> 'yaml'
            isFile() >> true
            getFile() >> tempFile
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 17, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        jobs.load(opts, out)

        then:
        1 * api.loadJobs('ProjectName', _, 'yaml', _, _) >>
                Calls.response(new ImportResult(succeeded: [], skipped: [], failed: [
                        new JobLoadItem(error: 'Test Error', name: 'Job Name')
                ]
                )
                )
        0 * api._(*_)
        1 * out.info('1 Jobs Failed:\n')
        1 * out.output(['[id:?] Job Name\n\t:Test Error'])
        0 * out._(*_)

    }


}
