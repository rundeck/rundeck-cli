package org.rundeck.client.api.model

import spock.lang.Specification

class PagingSpec extends Specification {
    def "tostring"() {
        expect:
        expect == new Paging(max: max, total: total, count: count, offset: offset).toString()
        where:
        max | total | count | offset | expect
        20  | 0     | 0     | 0      | 'Page [1/0] results 1 - 0 (of 0 by 20)'
        20  | 1     | 1     | 0      | 'Page [1/1] results 1 - 1 (of 1 by 20)'
        0   | 1     | 1     | 0      | 'Page [1/1] results 1 - 1 (of 1 by 0)'
    }
}
