package org.rundeck.client.util

import spock.lang.Specification

/**
 * Created by greg on 9/9/16.
 */
class RedirectBypassInterceptorSpec extends Specification {
    def "remap"() {
        when:
        String result = RedirectBypassInterceptor.remapUrl(orig, bypass, app)


        then:
        result == expect

        where:
        orig                    | bypass            | app               | expect
        "http://host1/c1/path1" | "http://host1"    | "http://host2/c2" | "http://host2/c2/c1/path1"
        "http://host1/c1/path1" | "http://host1/c1" | "http://host2/c2" | "http://host2/c2/path1"
        "http://host1/c1/path1" | "http://host1/c1" | "http://host2"    | "http://host2/path1"
    }
}
