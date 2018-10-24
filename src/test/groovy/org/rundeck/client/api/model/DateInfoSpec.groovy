package org.rundeck.client.api.model

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author greg
 * @since 8/24/17
 */
class DateInfoSpec extends Specification {
    @Unroll
    def "parse input correctly"() {
        given:
        when:
        DateInfo info = new DateInfo(input)
        then:
        info.toDate().time == time

        where:
            input                           | time
            '2017-08-24T22:43:18Z'          | 1503614598000
            '2017-08-24T15:43:18-07'        | 1503614598000
            '2017-08-24T15:43:18-0700'      | 1503614598000
            '2017-08-24T15:43:18-07:00'     | 1503614598000
            '2017-08-24T15:43:18-0730'      | 1503616398000
            '2017-08-24T15:43:18-07:30'     | 1503616398000
            '2017-08-24T15:43:18.900-07:30' | 1503616398900
    }

    def "toRelative"() {
        given:
            DateInfo info = new DateInfo('2017-08-24T15:43:18.000-07:30')
            Date relDate = new Date(tstamp)
        when:
            def result = info.toRelative(relDate)
        then:
            result == expected

        where:
            tstamp                                    | expected
            1503616398_000                            | "now"
            1503616399_000                            | "1s ago"
            1503616408_000                            | "10s ago"
            1503616458_000                            | "1m ago"
            1503619997_000                            | "59m ago"
            1503619998_000                            | "1h ago"
            (1503616398_000 + 23 * 3600 * 1000)       | "23h ago"
            (1503616398_000 + 24 * 3600 * 1000)       | "1d ago"
            (1503616398_000 + 20L * 24 * 3600 * 1000) | "20d ago"

    }
}
