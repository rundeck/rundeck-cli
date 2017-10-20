package org.rundeck.client.api.model

import spock.lang.Specification

/**
 * @author greg
 * @since 10/3/17
 */
class ExecLogSpec extends Specification {
    def "decompact"() {
        given:
        ExecLog orig = new ExecLog()
        orig.log = 'test1'
        orig.time = '13:02'
        orig.user = 'bob'
        orig.node = 'asdf'
        orig.level = 'INFO'

        ExecLog compact = new ExecLog()
        compact."$prop" = val

        when:
        def result = compact.decompact(orig)

        then:
        result.log == expect.log
        result.time == expect.time
        result.user == expect.user
        result.node == expect.node
        result.level == expect.level
        result.command == expect.command

        where:

        prop      | val     | expect
        'log'    | 'test2'    | [time: '13:02', user: 'bob', node: 'asdf', level: 'INFO', log: 'test2']
        'time'    | null    | [time: '13:02', user: 'bob', node: 'asdf', level: 'INFO', log: 'test1']
        'time'    | '123'   | [time: '123', user: 'bob', node: 'asdf', level: 'INFO', log: 'test1']
        'user'    | 'ziggy' | [time: '13:02', user: 'ziggy', node: 'asdf', level: 'INFO', log: 'test1']
        'node'    | 'node2' | [time: '13:02', user: 'bob', node: 'node2', level: 'INFO', log: 'test1']
        'level'   | 'DEBUG' | [time: '13:02', user: 'bob', node: 'asdf', level: 'DEBUG', log: 'test1']
        'command' | 'fddff' | [time: '13:02', user: 'bob', node: 'asdf', level: 'INFO', log: 'test1', command: 'fddff']
    }
}
