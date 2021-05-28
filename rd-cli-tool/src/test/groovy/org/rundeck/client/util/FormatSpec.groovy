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
        start | end | format        | data       | expected
        '${'  | '}' | '${a} b c'    | [a: 'x']   | 'x b c'
        '${'  | '}' | 'a ${b} c'    | [a: 'x']   | 'a  c'
        '${'  | '}' | 'a ${b} c'    | [b: 'x']   | 'a x c'
        '${'  | '}' | 'a ${b} ${c}' | [b: 'x']   | 'a x '
        '%'   | ''  | 'a %b %c'     | [b: 'x']   | 'a x '
        '%'   | ''  | 'a %b %c'     | [b: '$x']  | 'a $x '
        '%'   | ''  | 'a %b %c'     | [b: '\\x'] | 'a \\x '
        '%'   | ''  | 'a %b-x q'    | ['b-x': 'z'] | 'a z q'
        '%'   | ''  | 'a %b_x r'    | ['b_x': 'z'] | 'a z r'

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
        '%'   | ''  | '%a.b b c'     | [a: 'x', b: [c: 'd']] | ' b c'
        '%'   | ''  | '%b.c-d q r'     | [a: 'x', b: ['c-d': 'e']] | 'e q r'
        '%'   | ''  | '%b.c_d q r'     | [a: 'x', b: ['c_d': 'e']] | 'e q r'

    }
}
