package org.rundeck.client.tool.commands

import com.simplifyops.toolbelt.CommandOutput
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.DeleteJobsResult
import org.rundeck.client.api.model.JobItem
import org.rundeck.client.api.model.ScheduledJobItem
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 12/13/16
 */
class JobsSpec extends Specification {
    File tempFile = File.createTempFile('data', 'out')

    def setup() {
        tempFile = File.createTempFile('data', 'out')
    }

    def cleanup() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    def "job list with input parameters"() {
        given:
        def api = Mock(RundeckApi)

        def opts = Mock(Jobs.ListOpts) {
            isJob() >> true
            isProject() >> true
            getProject() >> 'ProjectName'
            getJobExact() >> jobexact
            getGroupExact() >> groupexact
            getJob() >> job
            getGroup() >> group
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 17)
        def hasclient = Mock(HasClient) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        jobs.list(opts, out)

        then:
        1 * api.listJobs('ProjectName', job, group, jobexact, groupexact) >>
                Calls.response([new JobItem(id: 'fakeid')])
        0 * api._(*_)

        where:
        job  | group | jobexact | groupexact
        'a'  | 'b/c' | null     | null
        null | null  | 'a'      | 'b/c'
    }

    def "job list write to file with input parameters"() {
        given:
        def api = Mock(RundeckApi)
        def opts = Mock(Jobs.ListOpts) {
            isJob() >> true
            isProject() >> true
            getProject() >> 'ProjectName'
            getJobExact() >> jobexact
            getGroupExact() >> groupexact
            getJob() >> job
            getGroup() >> group
            isFile() >> true
            getFormat() >> 'xml'
            getFile() >> tempFile
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 17)
        def hasclient = Mock(HasClient) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        jobs.list(opts, out)

        then:
        1 * api.exportJobs('ProjectName', job, group, jobexact, groupexact, 'xml') >>
                Calls.response(ResponseBody.create(MediaType.parse('application/xml'), 'abc'))
        0 * api._(*_)
        tempFile.exists()
        tempFile.text == 'abc'

        where:
        job  | group | jobexact | groupexact
        'a'  | 'b/c' | null     | null
        null | null  | 'a'      | 'b/c'
    }


    def "job purge with input parameters"() {
        given:
        def api = Mock(RundeckApi)

        def opts = Mock(Jobs.Purge) {
            isJob() >> true
            isProject() >> true
            getProject() >> 'ProjectName'
            getJobExact() >> jobexact
            getGroupExact() >> groupexact
            getJob() >> job
            getGroup() >> group
            isConfirm() >> true
        }
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 17)
        def hasclient = Mock(HasClient) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)
        when:
        def result = jobs.purge(opts, out)

        then:
        1 * api.listJobs('ProjectName', job, group, jobexact, groupexact) >>
                Calls.response([new JobItem(id: 'fakeid')])
        1 * api.deleteJobs(['fakeid']) >> Calls.response(new DeleteJobsResult(allsuccessful: true))
        0 * api._(*_)
        result

        where:
        job  | group | jobexact | groupexact
        'a'  | 'b/c' | null     | null
        null | null  | 'a'      | 'b/c'
    }

    def "jobs info outformat"() {
        given:

        def api = Mock(RundeckApi)
        def opts = Mock(Jobs.InfoOpts) {
            getId() >> "123"
            getOutputFormat() >> outFormat
            isOutputFormat() >> true
        }

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, 18)
        def hasclient = Mock(HasClient) {
            getClient() >> client
        }
        Jobs jobs = new Jobs(hasclient)
        def out = Mock(CommandOutput)

        when:
        jobs.info(opts, out)

        then:
        1 * api.getJobInfo('123') >> Calls.response(new ScheduledJobItem(id: '123', href: 'monkey'))
        1 * out.output([result])


        where:
        outFormat   | result
        '%id %href' | '123 monkey'
    }
}
