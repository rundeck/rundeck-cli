package org.rundeck.client.api.model

import spock.lang.Specification

/**
 * @author greg
 * @since 10/3/17
 */
class ExecOutputSpec extends Specification {
    def "decompact entries"() {
        given:
        ExecOutput logs = new ExecOutput()
        logs.compacted = true
        logs.entries = [new ExecLog(), new ExecLog(), new ExecLog()]
        logs.entries[0].level = 'INFO'
        logs.entries[0].log = 'test1'
        logs.entries[0].user = 'user1'
        logs.entries[0].node = 'node1'
        logs.entries[0].time = '13:01'

        logs.entries[1].log = 'test2'
        logs.entries[2].log = 'test3'

        when:
        def vals = logs.decompactEntries()
        then:
        vals != null
        vals.size() == logs.entries.size()
        vals[0].toMap() == logs.entries[0].toMap()
        vals[1].toMap() == logs.entries[0].toMap() + [log: 'test2']
        vals[2].toMap() == logs.entries[0].toMap() + [log: 'test3']

    }
}
