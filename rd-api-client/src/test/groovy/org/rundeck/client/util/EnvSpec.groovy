package org.rundeck.client.util

import spock.lang.Specification

class EnvSpec extends Specification {
    def "env keys are uppercased"() {
        given:
            def env = new Env()
            env.getter = { it }
        expect:
            expected == env.get(input)
        where:
            input        | expected
            'a'          | 'A'
            'a.b'        | 'A_B'
            'some.value' | 'SOME_VALUE'
            'SOME_VALUE' | 'SOME_VALUE'
    }
}
