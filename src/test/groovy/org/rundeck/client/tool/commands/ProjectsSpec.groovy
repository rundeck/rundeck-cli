package org.rundeck.client.tool.commands

import com.simplifyops.toolbelt.CommandOutput
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.ProjectItem
import org.rundeck.client.api.model.ScheduledJobItem
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 2/2/17
 */
class ProjectsSpec extends Specification {

    def "projects list outformat"() {
        given:

        def api = Mock(RundeckApi)
        def opts = Mock(Projects.ProjectListOpts) {
            getOutputFormat() >> outFormat
            isOutputFormat() >> (outFormat != null)
        }

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 18)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Projects projects = new Projects(hasclient)
        def out = Mock(CommandOutput)

        when:
        projects.list(opts, out)

        then:
        1 * api.listProjects() >>
                Calls.response(
                        [new ProjectItem(name: 'abc', description: '123', config: [xyz: '993']), new ProjectItem(
                                name: 'def',
                                description: ''
                        )]
                )
        1 * out.output(result)


        where:
        outFormat            | result
        null                 | ['abc', 'def']
        '%name'              | ['abc', 'def']
        '%name/%description' | ['abc/123', 'def/']
        '%name/%config'      | ['abc/{xyz=993}', 'def/']
        '%name/%config.xyz'  | ['abc/993', 'def/']
    }
}
