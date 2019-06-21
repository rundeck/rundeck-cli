package org.rundeck.client.api.model

import org.rundeck.client.util.RdClientConfig
import spock.lang.Specification
import spock.lang.Unroll
import sun.security.util.AuthResources_fr

class ExecutionSpec extends Specification {
    @Unroll
    def "get info map has job #hasJob"() {
        given:
            def jobmap = [
                    id             : 'jobid',
                    name           : 'jobname',
                    group          : 'agroup',
                    project        : 'aproject',
                    description    : 'blah',
                    href           : 'http://href',
                    permalink      : 'http://permalink',
                    averageDuration: 123l
            ]
            def job = hasJob ? new JobItem(jobmap) : null
            def e = new Execution(id: '1', href: 'http://no', description: '', job: job)
            def config = Mock(RdClientConfig)
        when:
            def result = e.getInfoMap(config)
        then:
            result
        where:
            hasJob << [true, false]
    }
}
