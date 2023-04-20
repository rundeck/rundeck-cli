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

package org.rundeck.client.tool.commands.projects


import okhttp3.ResponseBody
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ScmActionInputsResult
import org.rundeck.client.api.model.ScmActionPerform
import org.rundeck.client.api.model.ScmActionResult
import org.rundeck.client.api.model.ScmExportItem
import org.rundeck.client.api.model.ScmImportItem
import org.rundeck.client.api.model.ScmInputField
import org.rundeck.client.api.model.ScmJobItem
import org.rundeck.client.api.model.ScmProjectStatusResult
import org.rundeck.client.api.model.ScmSynchState
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.commands.RdToolImpl
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.VerboseOption
import org.rundeck.client.util.RdClientConfig
import org.rundeck.client.tool.Main
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author greg
 * @since 1/11/17
 */
class SCMSpec extends Specification {

    private RdTool setupMock(RundeckApi api, int apiVersion = 18) {
        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, apiVersion, true, new Main.OutputLogger(out))

        def appConfig = Mock(RdClientConfig)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
            getOutput() >> out
        }
        def rdTool = new RdToolImpl(rdapp)
        return rdTool
    }

    def "perform with validation response"() {
        given:
        def api = Mock(RundeckApi)
        def rdTool = setupMock(api)
        def out = rdTool.getRdApp().getOutput()
        def scm = new SCM()
        scm.rdTool = rdTool
        scm.rdOutput = out


        def baseopts = new SCM.BaseOpts()
        baseopts.project = 'aproject'
        baseopts.integration = 'export'
        def opts = new SCM.ActionPerformOptions()
        opts.action = 'project-commit'

        when:
        def result = scm.perform(baseopts, opts)
        then:

        1 * api.performScmAction('aproject', 'export', 'project-commit', _) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(
                                '''{"message":"Some input values were not valid.",
"nextAction":null,"success":false, "validationErrors":{"message":"required"}} ''',
                                Client.MEDIA_TYPE_JSON
                        )
                        )
                )
        1 * out.error("Action project-commit failed")
        1 * out.warning("Some input values were not valid.")
        1 * out.output([message: 'required'])
    }

    @Unroll
    def "perform import with include flags"() {
        given:
        def api = Mock(RundeckApi)
        def rdTool = setupMock(api)
        def out = rdTool.getRdApp().getOutput()
        def scm = new SCM()
        scm.rdTool = rdTool
        scm.rdOutput = out

        def baseopts = new SCM.BaseOpts()
        baseopts.project = 'aproject'
        baseopts.integration = 'import'

        def opts = new SCM.ActionPerformOptions()
        opts.action = 'import-all'
        opts.allItems = all
        opts.allTrackedItems = tracked
        opts.allUntrackedItems = untracked

        def items = [
                new ScmImportItem(itemId: 'a', tracked: true),
                new ScmImportItem(itemId: 'b', tracked: false),
                new ScmImportItem(itemId: 'c', tracked: true, deleted: true, job: new ScmJobItem(jobId: 'ajob')),
                new ScmImportItem(itemId: 'd', tracked: false, deleted: true, job: new ScmJobItem(jobId: 'xjob')),
        ]
        when:
        def result = scm.perform(baseopts, opts)
        then:

        1 * api.getScmActionInputs('aproject', 'import', 'import-all') >>
                Calls.response(
                        new ScmActionInputsResult(integration: 'import', actionId: 'import-all', importItems: items)
                )

        1 * api.performScmAction('aproject', 'import', 'import-all', { ScmActionPerform arg ->
            arg.items == expectedItems
            arg.deletedJobs==expectedDeletedJobs
        }
        ) >>
                Calls.response(
                        new ScmActionResult(success: true)
                )

        0 * api._(*_)
        where:
        all   | tracked | untracked | expectedItems | expectedDeletedJobs
        true  | false   | false     | ['a', 'b']    | ['ajob','xjob']
        false | true    | false     | ['a']         | ['ajob']
        false | false   | true      | ['b']         | ['xjob']
    }

    def "perform export with include flags"() {
        given:
        def api = Mock(RundeckApi)
        def rdTool = setupMock(api)
        def out = rdTool.getRdApp().getOutput()
        def scm = new SCM()
        scm.rdTool = rdTool
        scm.rdOutput = out

        def baseopts = new SCM.BaseOpts()
        baseopts.project = 'aproject'
        baseopts.integration = 'export'


        def opts = new SCM.ActionPerformOptions()
        opts.action = 'export-all'
        opts.allItems = all
        opts.allModifiedItems = modified
        opts.allDeletedItems = deleted

        def items = [
                new ScmExportItem(itemId: 'a', deleted: false),
                new ScmExportItem(itemId: 'b', deleted: true),
        ]
        when:
        def result = scm.perform(baseopts, opts)
        then:

        1 * api.getScmActionInputs('aproject', 'export', 'export-all') >>
                Calls.response(
                        new ScmActionInputsResult(integration: 'export', actionId: 'export-all', exportItems: items)
                )

        1 * api.performScmAction('aproject', 'export', 'export-all', { ScmActionPerform arg ->
            arg.items == expected && arg.deleted == expectedDeleted
        }
        ) >>
                Calls.response(
                        new ScmActionResult(success: true)
                )
        0 * api._(*_)
        result==0

        where:
        all   | modified | deleted | expected | expectedDeleted
        true  | false    | false   | ['a']    | ['b']
        false | true     | false   | ['a']    | []
        false | false    | true    | []       | ['b']
    }
    def "perform action failure"() {
        given:
        def api = Mock(RundeckApi)
        def rdTool = setupMock(api)
        def out = rdTool.getRdApp().getOutput()
        def scm = new SCM()
        scm.rdTool = rdTool
        scm.rdOutput = out

        def baseopts = new SCM.BaseOpts()
        baseopts.project = 'aproject'
        baseopts.integration = 'export'


        def opts = new SCM.ActionPerformOptions()
        opts.action = 'export-all'
        opts.allItems = all
        opts.allModifiedItems = modified
        opts.allDeletedItems = deleted

        def items = [
                new ScmExportItem(itemId: 'a', deleted: false),
                new ScmExportItem(itemId: 'b', deleted: true),
        ]
        when:
        def result = scm.perform(baseopts, opts)
        then:

        1 * api.getScmActionInputs('aproject', 'export', 'export-all') >>
                Calls.response(
                        new ScmActionInputsResult(integration: 'export', actionId: 'export-all', exportItems: items)
                )

        1 * api.performScmAction('aproject', 'export', 'export-all', { ScmActionPerform arg ->
            arg.items == expected && arg.deleted == expectedDeleted
        }
        ) >>
                Calls.response(
                        new ScmActionResult(success: false)
                )
        0 * api._(*_)
        result==1

        where:
        all   | modified | deleted | expected | expectedDeleted
        true  | false    | false   | ['a']    | ['b']
        false | true     | false   | ['a']    | []
        false | false    | true    | []       | ['b']
    }
    def "command inputs for import with null job"() {
        given:
        def api = Mock(RundeckApi)
        def rdTool = setupMock(api)
        def out = rdTool.getRdApp().getOutput()
        def scm = new SCM()
        scm.rdTool = rdTool
        scm.rdOutput = out

        def baseopts = new SCM.BaseOpts()
        baseopts.project = 'aproject'
        baseopts.integration = 'import'


        def opts = new SCM.ActionPerformOptions()
        opts.action = 'import-all'


        when:
        def result = scm.inputs(baseopts, opts, new VerboseOption())

        then:

        1 * api.getScmActionInputs('aproject', 'import', 'import-all') >>
                Calls.response(
                        new ScmActionInputsResult(
                                title: 'blah',
                                description: 'blah',
                                integration: 'import',
                                actionId: 'import-all',
                                fields: [new ScmInputField(
                                        name: 'commitMessage',
                                        description: 'abc',
                                        required: true,
                                        defaultValue: null,
                                        title: 'Message',
                                        type: 'String'
                                )],
                                importItems: [
                                        new ScmImportItem(itemId: '/a/path', tracked: false, job: null,deleted: false, status:'CLEAN'),
                                        new ScmImportItem(
                                                itemId: '/b/path',
                                                tracked: true,
                                                deleted: false,
                                                status:'CLEAN',
                                                job: new ScmJobItem(
                                                        jobId: 'ajob',
                                                        jobName: 'job name',
                                                        groupPath: 'monkey/banana'
                                                )
                                        )
                                ]
                        )
                )

        1 * out.output('blah: blah')
        1 * out.output('Fields:')
        1 * out.output(
                [['defaultValue'    : null,
                  'scope'           : null,
                  'values'          : null,
                  'name'            : 'commitMessage',
                  'description'     : 'abc',
                  'title'           : 'Message',
                  'renderingOptions': null,
                  'required'        : true]]
        )
        1 * out.output('Items:')
        1 * out.output(
                [
                        ['itemId': '/a/path', 'tracked': false, 'deleted': false, status:'CLEAN'],
                        ['itemId': '/b/path', 'tracked': true, 'deleted': false, status:'CLEAN',
                         'job'   : [
                                 'jobName'  : 'job name',
                                 'jobId'    : 'ajob',
                                 'groupPath': 'monkey/banana'
                         ]]
                ]
        )
    }

    def "scm status use project from env var"() {
        given:
        def api = Mock(RundeckApi)
        def rdTool = setupMock(api)
        def out = rdTool.getRdApp().getOutput()
        def appConfig = rdTool.appConfig
        def scm = new SCM()
        scm.rdTool = rdTool
        scm.rdOutput = out

        def baseopts = new SCM.BaseOpts()
        baseopts.project = null
        baseopts.integration = 'import'


        when:
        def result = scm.status(baseopts)

        then:
        result==0

        1 * appConfig.require('RD_PROJECT', _) >> 'TestProject'

        1 * api.getScmProjectStatus('TestProject', 'import') >>
                Calls.response(
                        new ScmProjectStatusResult(actions: [], message: 'test', synchState: ScmSynchState.CLEAN)
                )
        1 * out.output([message: 'test', actions: [], synchState: 'CLEAN'])
    }
    def "scm status not clean exit code"() {
        given:
        def api = Mock(RundeckApi)
        def rdTool = setupMock(api)
        def out = rdTool.getRdApp().getOutput()
        def appConfig = rdTool.appConfig
        def scm = new SCM()
        scm.rdTool = rdTool
        scm.rdOutput = out

        def baseopts = new SCM.BaseOpts()
        baseopts.project = null
        baseopts.integration = 'import'


        when:
        def result = scm.status(baseopts)

        then:

        1 * appConfig.require('RD_PROJECT', _) >> 'TestProject'

        1 * api.getScmProjectStatus('TestProject', 'import') >>
                Calls.response(
                        new ScmProjectStatusResult(actions: [], message: 'test', synchState: ScmSynchState.IMPORT_NEEDED)
                )

        result==1
    }
}
