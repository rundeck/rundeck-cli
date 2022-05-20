package testing.tests

import okhttp3.MediaType
import okhttp3.RequestBody
import org.rundeck.client.RundeckClient
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.CreateToken
import org.rundeck.client.api.model.ImportResult
import org.rundeck.client.api.model.ProjectItem
import org.rundeck.client.util.Client
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.Transferable
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

@Testcontainers
class BasicIntegrationSpec extends Specification {
    @Shared
    GenericContainer rundeck = new GenericContainer<>("rundeck/rundeck:4.2.1")
            .withEnv([
                    RUNDECK_TOKENS_FILE: '/var/lib/rundeck/etc/tokens.properties'
            ])
            .withCopyToContainer(
                    Transferable.of('admin=letmeinplease,admin'),
                    '/var/lib/rundeck/etc/tokens.properties',
            )
            .waitingFor(Wait.forHttp("/api/41/system/info").forStatusCode(403).withStartupTimeout(Duration.ofMinutes(5)))
//            .waitingFor(
//                    Wait.forLogMessage(".*rundeckapp.Application - Started Application.*\\n", 1)
//            )
            .withExposedPorts(4440)

    @Shared
    Client<RundeckApi> client

    static String PROJ_NAME = 'test'
    static String JOBFILE1 = 'jobs/test_job.xml'
    static String JOBFILE2 = 'jobs/test_job2.yaml'
    static class TestLogger implements Client.Logger {
        @Override
        void output(String out) {
            println(out)
        }

        @Override
        void warning(String warn) {
            System.err.println(warn)
        }

        @Override
        void error(String err) {
            System.err.println(err)
        }
    }

    def setup() {
        String address = rundeck.host
        Integer port = rundeck.getMappedPort(4440)
        def rdUrl = "http://${address}:${port}/api/41"
        System.err.println("rdUrl: $rdUrl")
        client = RundeckClient.builder().with {
            baseUrl rdUrl
            tokenAuth 'letmeinplease'// token.token
            logger(new TestLogger())
            build()
        }

        def projList = client.apiCall(api -> api.listProjects())

        if (!projList*.name.contains(PROJ_NAME)) {
            def project = client.apiCall(api -> api.createProject(new ProjectItem(name: PROJ_NAME)))
        }
    }

    def "Job import format #type"() {
        given:

        Path jobdefPath = Files.createTempFile("test-job1", ".temp")
        def jobdefFile = jobdefPath.toFile()
        jobdefFile.deleteOnExit()
        jobdefFile.newOutputStream() << this.class.classLoader.getResourceAsStream(fileName)

        when:
        ImportResult result = client.apiCall(api -> api.loadJobs(
                PROJ_NAME,
                RequestBody.create(
                        jobdefFile,
                        MediaType.get(type)
                )
        ))
        then:
        result != null
        result.succeeded.size() == 1
        where:
        fileName | type
        JOBFILE1 | 'application/xml'
        JOBFILE2 | 'text/yaml'
    }

}
