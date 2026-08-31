/*
 * Copyright 2026 Rundeck, Inc. (http://rundeck.com)
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

package org.rundeck.client

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.rundeck.client.util.RdClientConfig
import spock.lang.Specification

/**
 * Regression test for https://github.com/rundeck/rundeck-cli/issues/614 :
 * RD_BYPASS_URL was ignored because the CrossOriginRedirectInterceptor (added by
 * RD_ALLOW_CROSS_ORIGIN_REDIRECT handling) evaluated the raw, un-rewritten redirect
 * Location before RedirectBypassInterceptor got a chance to rewrite it back to the
 * app's own base URL, so legitimate bypass redirects were blocked as if they were
 * cross-origin.
 */
class RundeckClientRedirectBypassSpec extends Specification {
    MockWebServer server

    def setup() {
        server = new MockWebServer()
        server.start()
    }

    def cleanup() {
        server.shutdown()
    }

    private RdClientConfig configWithBypassUrl(final String bypassUrl) {
        Stub(RdClientConfig) {
            getString(_, _) >> {
                String key, String defval -> key == RundeckClient.ENV_BYPASS_URL ? bypassUrl : defval
            }
            getBool(_, _) >> { String key, boolean defval -> defval }
            getLong(_, _) >> { String key, Long defval -> defval }
            getInt(_, _) >> { String key, int defval -> defval }
            getDebugLevel() >> 0
        }
    }

    def "redirect to RD_BYPASS_URL is rewritten and not blocked as cross-origin"() {
        given: "a client configured with RD_BYPASS_URL pointing at an external host"
        def baseUrl = server.url("/").toString()
        def bypassUrl = "https://myhost.example.com"

        def builder = RundeckClient.builder()
                                    .baseUrl(baseUrl)
                                    .config(configWithBypassUrl(bypassUrl))

        OkHttpClient client = builder.okhttp.build()

        server.enqueue(
                new MockResponse()
                        .setResponseCode(302)
                        .setHeader("Location", "${bypassUrl}/api/29/system/info")
        )
        server.enqueue(new MockResponse().setResponseCode(200).setBody('ok'))

        def request = new Request.Builder().url("${baseUrl}api/29/system/info").build()

        when: "the server redirects to the bypass URL"
        def response = client.newCall(request).execute()

        then: "the redirect is rewritten back to the local base URL and followed successfully"
        response.code() == 200
        response.body().string() == 'ok'
    }

    def "redirect to an unrelated host is still blocked even when RD_BYPASS_URL is set"() {
        given: "a client configured with RD_BYPASS_URL, but the redirect points elsewhere"
        def baseUrl = server.url("/").toString()
        def bypassUrl = "https://myhost.example.com"

        def builder = RundeckClient.builder()
                                    .baseUrl(baseUrl)
                                    .config(configWithBypassUrl(bypassUrl))

        OkHttpClient client = builder.okhttp.build()

        server.enqueue(
                new MockResponse()
                        .setResponseCode(302)
                        .setHeader("Location", "http://attacker.example.com/capture")
        )

        def request = new Request.Builder().url("${baseUrl}api/29/system/info").build()

        when:
        client.newCall(request).execute()

        then:
        IOException e = thrown()
        e.message.contains('Cross-origin redirect blocked')
    }
}
