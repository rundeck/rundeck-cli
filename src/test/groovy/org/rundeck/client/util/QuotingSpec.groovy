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
