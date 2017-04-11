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

import static org.rundeck.client.util.Quoting.joinStringQuoted
import static org.rundeck.client.util.Quoting.needsQuoting
import static org.rundeck.client.util.Quoting.quoteString

/**
 * Created by greg on 10/21/16.
 */
class QuotingSpec extends Specification {
    def "JoinString"() {

        given:
        def string = joinStringQuoted(strings)

        expect:
        string == result

        where:
        strings            | result
        ["a", "b"]         | "a b"
        ["a b", "c"]       | "\"a b\" c"
        ["a \"b\"", "c"]   | "\"a \"\"b\"\"\" c"
        ["a\"b\"", "c"]    | "\"a\"\"b\"\"\" c"
        ["'a \"b\"'", "c"] | "'a \"b\"' c"
        ["\"a 'b'\"", "c"] | "\"a 'b'\" c"

    }

    def "needs quoting"() {
        given:
        def val = needsQuoting(string)

        expect:
        val == expected

        where:
        string        | expected
        "a"           | false
        "abcdef"      | false
        "abcdef'asdf" | true
        "a b"         | true
        "a 'b'"       | true
        "a \"b\""     | true
        "'a b'"       | false
        "\"a b\""     | false
    }

    def "quote string"() {
        given:
        def val = quoteString(string)

        expect:
        val == expected

        where:
        string          | expected
        'abc'           | '"abc"'
        '"abc"'         | '"""abc"""'
        'abc "xyz" def' | '"abc ""xyz"" def"'
    }
}
