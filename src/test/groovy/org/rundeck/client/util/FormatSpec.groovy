package org.rundeck.client.util

import spock.lang.Specification

/**
 * Created by greg on 11/17/16.
 */
class FormatSpec extends Specification {
    def "format"() {
        given:
        when:
        def result = Format.format(format, data, start, end)
        then:
        result == expected
        where:
        start | end | format        | data     | expected
        '${'  | '}' | '${a} b c'    | [a: 'x'] | 'x b c'
        '${'  | '}' | 'a ${b} c'    | [a: 'x'] | 'a  c'
        '${'  | '}' | 'a ${b} c'    | [b: 'x'] | 'a x c'
        '${'  | '}' | 'a ${b} ${c}' | [b: 'x'] | 'a x '
        '%'   | ''  | 'a %b %c'     | [b: 'x'] | 'a x '

    }

    def "format descend map"() {
        given:
        when:
        def result = Format.format(format, data, start, end)
        then:
        result == expected
        where:
        start | end | format         | data                  | expected
        '${'  | '}' | '${a} b c'     | [a: 'x', b: [c: 'd']] | 'x b c'
        '${'  | '}' | '${b.c} b c'   | [a: 'x', b: [c: 'd']] | 'd b c'
        '${'  | '}' | '${b.DNE} b c' | [a: 'x', b: [c: 'd']] | ' b c'
        '${'  | '}' | '${a.b} b c'   | [a: 'x', b: [c: 'd']] | ' b c'

    }
}
