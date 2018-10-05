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
import org.rundeck.client.api.model.repository.Artifact
import org.rundeck.client.api.model.repository.RepositoryArtifacts
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.toolbelt.CommandOutput
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification


class PluginsTest extends Specification {
    def "List"() {
        given:
        def api = Mock(RundeckApi)
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 26, true, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
        }
        Plugins plugins = new Plugins(rdapp)
        def out = Mock(CommandOutput)

        when:
        plugins.list(out)

        then:
        1 * api.listPlugins() >> Calls.response(listRepoResponse())
        out.output('1b7dc3be7836 : JNotify (installed)\nbcf8885df1e8 : Scripter (not installed)')

    }

    List<RepositoryArtifacts> listRepoResponse() {
        def list = []
        RepositoryArtifacts ra = new RepositoryArtifacts()
        ra.repositoryName = "private"
        ra.results = [new Artifact(id:"1b7dc3be7836",name: "JNotify",installed: true),
                      new Artifact(id:"bcf8885df1e8",name: "Scripter",installed: false)]
        return list
    }

}
