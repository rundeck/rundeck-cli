package org.rundeck.client.tool.commands

import com.simplifyops.toolbelt.CommandOutput
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.Execution
import org.rundeck.client.api.model.JobItem
import org.rundeck.client.tool.AppConfig
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.options.RunBaseOptions
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author greg
 * @since 12/13/16
 */
class RunSpec extends Specification {
    def "run command -j queries for exact job name and group"() {

        given:
        def api = Mock(RundeckApi)

        def opts = Mock(RunBaseOptions) {
            isJob() >> true
            isProject() >> true
            getProject() >> 'ProjectName'
            getJob() >> 'a group/path/a job'
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 17)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Run run = new Run(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = run.run(opts, out)

        then:
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, null, null, null) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result

    }

    @Unroll
    def "run gets project from ENV if specified = false #isproj"() {

        given:
        def api = Mock(RundeckApi)

        def opts = Mock(RunBaseOptions) {
            isJob() >> true
            isProject() >> isproj
            getProject() >> (isproj ? "ProjectName" : null)
            getJob() >> 'a group/path/a job'
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 17)
        def appConfig = Mock(AppConfig)
        def hasclient = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> appConfig
        }
        Run run = new Run(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = run.run(opts, out)

        then:
        if(!isproj) {
            1 * appConfig.require('RD_PROJECT', _) >> "ProjectName"
        }
        1 * api.listJobs('ProjectName', null, null, 'a job', 'a group/path') >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.runJob('fakeid', null, null, null, null) >> Calls.response(new Execution(id: 123, description: ''))
        0 * api._(*_)
        result

        where:
        isproj | _
        true   | _
        false  | _
    }
}
