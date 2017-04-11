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
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ScmProjectStatusResult
import org.rundeck.client.api.model.ScmSynchState
import org.rundeck.client.tool.AppConfig
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 1/11/17
 */
class SCMSpec extends Specification {
    def "scm status use project from env var"() {
        given:
        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 18)

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
