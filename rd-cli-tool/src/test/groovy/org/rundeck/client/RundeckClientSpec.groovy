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

package org.rundeck.client

import org.rundeck.client.util.FormAuthInterceptor
import spock.lang.Specification

/**
 * @author greg
 * @since 11/22/16
 */
class RundeckClientSpec extends Specification {
    def "create user and password valid"() {
        given:
        def user = 'user1'
        def pass = 'pass1'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                passwordAuth(user, pass).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        result != null

        where:
        proto   | _
        'http'  | _
        'https' | _
    }

    def "password auth with API vers has correct auth url"() {
        given:
        def root = 'https://example.com'
        when:

        def builder = RundeckClient.builder().
                baseUrl(root + basePath).
                passwordAuth('user1', 'pass1')

        then:
        builder.okhttp.interceptors().size() == 1
        def formAuth = builder.okhttp.interceptors().get(0)
        formAuth instanceof FormAuthInterceptor
        formAuth.baseUrl == (root + expectedBase)
        formAuth.j_security_url == (root + expectedSecurity)

        where:
        basePath          | expectedBase | expectedSecurity
        "/"               | "/"          | "/j_security_check"
        "/api/19"         | "/"          | "/j_security_check"
        "/context"        | "/context/"  | "/context/j_security_check"
        "/context/api/19" | "/context/"  | "/context/j_security_check"
    }

    def "create token valid"() {
        given:
        def token = 'abc'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                tokenAuth(token).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        result != null

        where:
        proto   | _
        'http'  | _
        'https' | _
    }

    def "create bearer token valid"() {
        given:
        def btoken = 'abc'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                bearerTokenAuth(btoken).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        result != null

        where:
        proto   | _
        'http'  | _
        'https' | _
    }

    def "create user and password invalid URL"() {
        given:
        def user = 'user1'
        def pass = 'pass1'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                passwordAuth(user, pass).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /Not a valid base URL/

        where:
        proto  | _
        'file' | _
        'blah' | _
    }

    def "create user and password invalid user"() {
        given:
        def pass = 'pass1'
        def baseUrl = "http://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                passwordAuth(user, pass).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /User cannot be blank or null/

        where:
        user | _
        ''   | _
        null | _
    }

    def "create user and password invalid password"() {
        given:
        def user = 'asdf'
        def baseUrl = "http://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                passwordAuth(user, pass).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /Password cannot be blank or null/

        where:
        pass | _
        ''   | _
        null | _
    }

    def "create token invalid URL"() {
        given:
        def token = 'atoken'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                tokenAuth(token).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /Not a valid base URL/

        where:
        proto  | _
        'file' | _
        'blah' | _
    }

    def "create bearer token invalid URL"() {
        given:
        def token = 'atoken'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                bearerTokenAuth(token).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /Not a valid base URL/

        where:
        proto  | _
        'file' | _
        'blah' | _
    }

    def "create token invalid token"() {
        given:
        def baseUrl = "http://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                tokenAuth(token).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /Token cannot be blank or null/

        where:
        token | _
        ''    | _
        null  | _
    }

    def "create bearer token invalid token"() {
        given:
        def baseUrl = "http://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = RundeckClient.builder().
                baseUrl(baseUrl).
                bearerTokenAuth(token).
                logging(debuglevel).
                timeout(httpTimeout).
                retryConnect(retryConnect).
                build()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /Token cannot be blank or null/

        where:
        token | _
        ''    | _
        null  | _
    }
}
