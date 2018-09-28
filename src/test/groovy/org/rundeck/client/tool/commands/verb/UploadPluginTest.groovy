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
package org.rundeck.client.tool.commands.verb

import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.verb.ArtifactActionMessage
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.toolbelt.CommandOutput
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification


class UploadPluginTest extends Specification {
    def "Upload"() {
        given:
        File bogusPlugin = File.createTempFile("bogus","plugin")
        def api = Mock(RundeckApi)
        def opts = Mock(UploadPlugin.UploadPluginOption) {
            getBinaryPath() >> bogusPlugin.absolutePath
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 26, false, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
        }
        UploadPlugin uploadCmd = new UploadPlugin(rdapp)
        def out = Mock(CommandOutput)

        when:
        uploadCmd.upload(opts,out)

        then:
        1 * api.uploadPlugin(_) >> Calls.response(new ArtifactActionMessage(msg:"Upload succeeded"))
        out.output('Upload succeeded')
    }
}
