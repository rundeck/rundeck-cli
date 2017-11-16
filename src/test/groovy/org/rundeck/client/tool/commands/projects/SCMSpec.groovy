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

import com.simplifyops.toolbelt.CommandOutput
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
import org.rundeck.client.tool.AppConfig
import org.rundeck.client.tool.Main
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 1/11/17
 */
class SCMSpec extends Specification {
    def "perform with validation response"() {
        given:
        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, new Main.OutputLogger(out))

        def appConfig = Mock(AppConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        def scm = new SCM(hasclient)

        def opts = Mock(SCM.ActionPerformOptions) {
            getProject() >> 'aproject'
            getIntegration() >> 'export'
            getAction() >> 'project-commit'
        }
        when:
        def result = scm.perform(opts, out)
        then:

        1 * api.performScmAction('aproject', 'export', 'project-commit', _) >>
                Calls.response(
                        Response.error(400, ResponseBody.create(Client.MEDIA_TYPE_JSON,
                                                                '''{"message":"Some input values were not valid.",
"nextAction":null,"success":false, "validationErrors":{"message":"required"}} '''
                        )
                        )
                )
        1 * out.error("Action project-commit failed")
        1 * out.warning("Some input values were not valid.")
        1 * out.output([message: 'required'])
    }

    def "perform import with include flags"() {
        given:
        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, new Main.OutputLogger(out))

        def appConfig = Mock(AppConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        def scm = new SCM(hasclient)

        def opts = Mock(SCM.ActionPerformOptions) {
            getProject() >> 'aproject'
            getIntegration() >> 'import'
            getAction() >> 'import-all'
            isAllItems() >> all
            isAllTrackedItems() >> tracked
            isAllUntrackedItems() >> untracked
        }
        def items = [
                new ScmImportItem(itemId: 'a', tracked: true),
                new ScmImportItem(itemId: 'b', tracked: false),
        ]
        when:
        def result = scm.perform(opts, out)
        then:

        1 * api.getScmActionInputs('aproject', 'import', 'import-all') >>
                Calls.response(
                        new ScmActionInputsResult(integration: 'import', actionId: 'import-all', importItems: items)
                )

        1 * api.performScmAction('aproject', 'import', 'import-all', { ScmActionPerform arg ->
            arg.items == expected
        }
        ) >>
                Calls.response(
                        new ScmActionResult(success: true)
                )

        0 * api._(*_)
        where:
        all   | tracked | untracked | expected
        true  | false   | false     | ['a', 'b']
        false | true    | false     | ['a']
        false | false   | true      | ['b']
    }

    def "perform export with include flags"() {
        given:
        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, new Main.OutputLogger(out))

        def appConfig = Mock(AppConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        def scm = new SCM(hasclient)

        def opts = Mock(SCM.ActionPerformOptions) {
            getProject() >> 'aproject'
            getIntegration() >> 'export'
            getAction() >> 'export-all'
            isAllItems() >> all
            isAllModifiedItems() >> modified
            isAllDeletedItems() >> deleted
        }
        def items = [
                new ScmExportItem(itemId: 'a', deleted: false),
                new ScmExportItem(itemId: 'b', deleted: true),
        ]
        when:
        def result = scm.perform(opts, out)
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

        where:
        all   | modified | deleted | expected | expectedDeleted
        true  | false    | false   | ['a']    | ['b']
        false | true     | false   | ['a']    | []
        false | false    | true    | []       | ['b']
    }
    def "command inputs for import with null job"() {
        given:
        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)

        def appConfig = Mock(AppConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        def scm = new SCM(hasclient)

        def out = Mock(CommandOutput)
        def opts = Mock(SCM.ActionInputsOptions) {
            getProject() >> 'aproject'
            getIntegration() >> 'import'
            getAction() >> 'import-all'
        }



        when:
        def result = scm.inputs(opts, out)

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
                                        new ScmImportItem(itemId: '/a/path', tracked: false, job: null),
                                        new ScmImportItem(
                                                itemId: '/b/path',
                                                tracked: true,
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
                        ['itemId': '/a/path', 'tracked': false],
                        ['itemId': '/b/path', 'tracked': true,
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

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)

        def appConfig = Mock(AppConfig)

        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        def scm = new SCM(hasclient)

        def out = Mock(CommandOutput)
        def opts = Mock(SCM.StatusOptions) {
            getIntegration() >> 'import'
        }


        when:
        def result = scm.status(opts, out)

        then:
        result

        1 * opts.getProject() >> null
        1 * appConfig.require('RD_PROJECT', _) >> 'TestProject'

        1 * api.getScmProjectStatus('TestProject', 'import') >>
                Calls.response(
                        new ScmProjectStatusResult(actions: [], message: 'test', synchState: ScmSynchState.CLEAN)
                )
        1 * out.output([message: 'test', actions: [], synchState: 'CLEAN'])
    }
}
