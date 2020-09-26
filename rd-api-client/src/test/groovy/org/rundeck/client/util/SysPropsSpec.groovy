package org.rundeck.client.util

import spock.lang.Specification

class SysPropsSpec extends Specification {
    def "keys lowercased"() {
        given:
            SysProps props = new SysProps()
            props.getter = { a, b ->
                a
            }
        expect:
            expected == props.get(input)

        where:
            input           | expected
            'a'             | 'a'
            'a.b'           | 'a.b'
            'A_B'           | 'a.b'
            'SOME_PROP_KEY' | 'some.prop.key'
    }
}
