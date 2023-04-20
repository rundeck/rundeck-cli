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
package org.rundeck.client.tool.commands.repository

import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.repository.ArtifactActionMessage
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification


class InstallPluginTest extends Specification {
    def "Install"() {
        given:
        def api = Mock(RundeckApi)
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 26, false, null)
        def out = Mock(CommandOutput)
        def rdapp = Mock(RdApp) {
            getClient() >> client
            getOutput() >> out
        }
        def rdtool = new MockRdTool(client: client, rdApp: rdapp)

        InstallPlugin installCmd = new InstallPlugin()
        installCmd.rdTool = rdtool
        installCmd.rdOutput = out
        installCmd.repoName = 'private'
        installCmd.pluginId = 'bcf8885df1e8'

        when:
        def result = installCmd.call()

        then:
        1 * api.installPlugin("private", 'bcf8885df1e8') >> Calls.response(new ArtifactActionMessage(msg: "Plugin Installed"))
        1 * out.output('Plugin Installed')
        result == 0
    }
}
