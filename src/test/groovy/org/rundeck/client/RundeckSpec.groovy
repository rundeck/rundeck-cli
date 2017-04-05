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
