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
import org.rundeck.client.api.model.ProjectItem
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 2/2/17
 */
class ProjectsSpec extends Specification {

    def "projects list outformat"() {
        given:

        def api = Mock(RundeckApi)
        def opts = Mock(Projects.ProjectListOpts) {
            getOutputFormat() >> outFormat
            isOutputFormat() >> (outFormat != null)
        }

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 18, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Projects projects = new Projects(hasclient)
        def out = Mock(CommandOutput)

        when:
        projects.list(opts, out)

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
