package org.rundeck.client.tool.commands;

import junit.framework.TestCase;
import spock.lang.Specification;

class DefaultOktaApiProviderSpec extends Specification {
    def "basic auth string"() {
        when:
        def result = DefaultOktaApiProvider.basicAuthString(id, secret.toCharArray())
        then:
        result == expected
        where:
        id     | secret     | expected
        'user' | 'password' | 'dXNlcjpwYXNzd29yZA=='
        'asdf' | 'asdf'     | 'YXNkZjphc2Rm'
    }
}