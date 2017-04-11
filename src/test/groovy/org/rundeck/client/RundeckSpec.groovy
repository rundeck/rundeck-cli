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

import spock.lang.Specification

/**
 * @author greg
 * @since 11/22/16
 */
class RundeckSpec extends Specification {
    def "create user and password valid"() {
        given:
        def user = 'user1'
        def pass = 'pass1'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = Rundeck.builder().
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

    def "create token valid"() {
        given:
        def token = 'abc'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = Rundeck.builder().
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

    def "create user and password invalid URL"() {
        given:
        def user = 'user1'
        def pass = 'pass1'
        def baseUrl = "$proto://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = Rundeck.builder().
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

        def result = Rundeck.builder().
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

        def result = Rundeck.builder().
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

        def result = Rundeck.builder().
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

    def "create token invalid token"() {
        given:
        def baseUrl = "http://example.com"
        int debuglevel = 0
        long httpTimeout = 30
        boolean retryConnect = true
        when:

        def result = Rundeck.builder().
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
}
