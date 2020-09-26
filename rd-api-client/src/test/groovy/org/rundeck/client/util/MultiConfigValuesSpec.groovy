package org.rundeck.client.util

import spock.lang.Specification

class MultiConfigValuesSpec extends Specification {
    def "multi value"() {
        given:
            def a = new MapConfigValues([a: 'b'])
            def b = new MapConfigValues([b: 'c', a: 'z'])
            def c = new MapConfigValues([b: 'q'])
            def multi = new MultiConfigValues(a, b, c)

        when:
            def val = multi.get(input)

        then:
            val == expected

        where:
            input | expected
            'a'   | 'b'
            'b'   | 'c'
            'c'   | null

    }
}
