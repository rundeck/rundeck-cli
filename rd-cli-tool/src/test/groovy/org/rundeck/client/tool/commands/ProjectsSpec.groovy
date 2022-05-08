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
import org.rundeck.client.api.model.KeyStorageItem
import org.rundeck.client.api.model.ProjectItem
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.commands.projects.Configure
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.ProjectListFormatOptions
import org.rundeck.client.tool.options.ProjectNameOptions
import org.rundeck.client.tool.options.UnparsedConfigOptions
import org.rundeck.client.tool.options.VerboseOption
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 2/2/17
 */
class ProjectsSpec extends Specification {

    private RdTool setupMock(RundeckApi api, int apiVersion=18) {
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
    def "create does not require config"() {
        given:

        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Projects command = new Projects()
        command.rdTool=rdTool
        command.rdOutput=out



        when:
        command.create(new ProjectNameOptions(project: 'testProject'),new Configure.ConfigFileOptions(),new UnparsedConfigOptions())

        then:
        1 * api.createProject(_) >>
        Calls.response(new ProjectItem(name: 'testProject', description: '123', config: [:]))

    }
    def "projects list outformat"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Projects command = new Projects()
        command.rdTool=rdTool
        command.rdOutput=out

        command.verboseOption=new VerboseOption()
        command.formatOptions=new ProjectListFormatOptions()
        command.formatOptions.outputFormat=outFormat

        when:
        command.list()

        then:
        1 * api.listProjects() >>
                Calls.response(
                        [new ProjectItem(name: 'abc', description: '123', config: [xyz: '993']), new ProjectItem(
                                name: 'def',
                                description: ''
                        )]
                )
        1 * out.output(result)


        where:
        outFormat            | result
        null                 | ['abc', 'def']
        '%name'              | ['abc', 'def']
        '%name/%description' | ['abc/123', 'def/']
        '%name/%config'      | ['abc/{xyz=993}', 'def/']
        '%name/%config.xyz'  | ['abc/993', 'def/']
    }
}
