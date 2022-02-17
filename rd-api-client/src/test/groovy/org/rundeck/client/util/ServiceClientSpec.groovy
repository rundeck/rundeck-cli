package org.rundeck.client.util

import groovy.transform.CompileStatic
import okhttp3.MediaType
import spock.lang.Specification
import spock.lang.Unroll

class ServiceClientSpec extends Specification {
    @Unroll
    def "has any media type"() {
        given:
        def input = inputType ? MediaType.parse(inputType) : null
        def types = listTypes.collect { MediaType.parse(it) }
        expect:
        test == ServiceClient.hasAnyMediaType(input, types.toArray(new MediaType[types.size()]))
        where:

        test  | inputType     | listTypes
        true  | 'text/plain'  | ['text/plain']
        true  | 'text/plain'  | ['text/plain', 'data/binary']
        true  | 'data/binary' | ['text/plain', 'data/binary']
        false | 'text/other'  | ['text/plain']
        false | null          | ['text/plain']
    }
}
