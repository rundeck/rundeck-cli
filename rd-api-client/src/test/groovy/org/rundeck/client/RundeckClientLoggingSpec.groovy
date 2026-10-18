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
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * Debug HTTP logging (RD_DEBUG) must not write credentials to the log output
 */
class RundeckClientLoggingSpec extends Specification {
    static final String TOKEN = 'secret-token-value-123'
    static final String PASSWORD = 'secret-password-456'

    MockWebServer server
    Logger okhttpLogger
    List<String> logged
    Handler handler

    def setup() {
        server = new MockWebServer()
        server.start()
        logged = Collections.synchronizedList([])
        handler = new Handler() {
            @Override
            void publish(final LogRecord record) {
                logged << record.message
            }

            @Override
            void flush() {
            }

            @Override
            void close() throws SecurityException {
            }
        }
        okhttpLogger = Logger.getLogger(OkHttpClient.name)
        okhttpLogger.addHandler(handler)
    }

    def cleanup() {
        okhttpLogger.removeHandler(handler)
        server.shutdown()
    }

    @Unroll
    def "token auth header is redacted at debug level #level"() {
        given:
        def baseUrl = server.url('/').toString()
        OkHttpClient client = RundeckClient.builder()
                                           .baseUrl(baseUrl)
                                           .tokenAuth(TOKEN)
                                           .logging(level)
                                           .okhttp
                                           .build()
        server.enqueue(new MockResponse().setResponseCode(200).setHeader('Set-Cookie', 'JSESSIONID=abc123').setBody('ok'))

        when:
        def response = client.newCall(new Request.Builder().url("${baseUrl}api/41/system/info").build()).execute()
        response.body().string()

        then:
        server.takeRequest().getHeader('X-Rundeck-Auth-Token') == TOKEN
        logged.any { it.startsWith('X-Rundeck-Auth-Token: ██') }
        logged.any { it.startsWith('Set-Cookie: ██') }
        logged.every { !it.contains(TOKEN) && !it.contains('abc123') }

        where:
        level << [2, 3]
    }

    def "form auth password is redacted from the logged request body"() {
        given:
        def baseUrl = server.url('/').toString()
        OkHttpClient client = RundeckClient.builder()
                                           .baseUrl(baseUrl)
                                           .passwordAuth('admin', PASSWORD)
                                           .logging(3)
                                           .okhttp
                                           .build()
        server.enqueue(new MockResponse().setResponseCode(200).setBody('login page'))
        server.enqueue(new MockResponse().setResponseCode(200).setBody('logged in'))
        server.enqueue(new MockResponse().setResponseCode(200).setBody('ok'))

        when:
        def response = client.newCall(new Request.Builder().url("${baseUrl}api/41/system/info").build()).execute()
        response.body().string()

        then:
        server.takeRequest()
        server.takeRequest().body.readUtf8().contains("j_password=${PASSWORD}")
        logged.any { it.contains('j_username=admin&j_password=██') }
        logged.every { !it.contains(PASSWORD) }
    }

    @Unroll
    def "redacting logger masks password field in '#message'"() {
        given:
        List<String> out = []
        def logger = RundeckClient.redactingLogger({ String msg -> out << msg })

        when:
        logger.log(message)

        then:
        out == [expected]

        where:
        message                          | expected
        'j_username=a&j_password=s3cret' | 'j_username=a&j_password=██'
        'j_password=s3cret&j_username=a' | 'j_password=██&j_username=a'
        'x_j_password=keep'              | 'x_j_password=keep'
        '--> GET /api/41/system/info'    | '--> GET /api/41/system/info'
    }
}
