package org.rundeck.client.api.model

import spock.lang.Specification

/**
 * @author greg
 * @since 8/24/17
 */
class DateInfoSpec extends Specification {
    def "parse input correctly"() {
        given:
        when:
        DateInfo info = new DateInfo(input)
        then:
        info.toDate().time == time

        where:
        input                       | time
        '2017-08-24T22:43:18Z'      | 1503614598000
        '2017-08-24T15:43:18-07'    | 1503614598000
        '2017-08-24T15:43:18-0700'  | 1503614598000
        '2017-08-24T15:43:18-07:00' | 1503614598000
        '2017-08-24T15:43:18-0730'  | 1503616398000
        '2017-08-24T15:43:18-07:30' | 1503616398000
    }
}
