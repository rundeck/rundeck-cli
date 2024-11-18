package org.rundeck.client.tool.commands.projects


import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.AsyncProjectImportStatus
import org.rundeck.client.api.model.ProjectImportStatus
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.InputError
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.commands.RdToolImpl
import org.rundeck.client.tool.options.ProjectNameOptions
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification

class ArchivesSpec extends Specification {
    File tempFile

    def setup() {
        tempFile = File.createTempFile('ArchivesSpec-test', 'zip')
    }

    def cleanup() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    def "import component options set in url"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'


        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                [
                        'importComponents.test-comp': 'true',
                        'importOpts.test-comp.key'  : 'value',
                ],
                _
        ) >> Calls.response(new ProjectImportStatus(successful: true))
        0 * api._(*_)
        result == 0
    }

    def "api version < 34 fails for webhooks or webhook regen auth flag"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, apiversion, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'
        if (useIncludeFlags) {
            opts.setIncludeFlags(new HashSet<Archives.ImportFlags>([Archives.ImportFlags.webhooks]))
        } else {
            opts.includeWebhooks = true
        }
        opts.whkRegenAuthTokens = true


        when:
        def result = sut.importArchive(opts)

        then:
        InputError exc = thrown()
        exc.message.contains('Cannot use --include-webhooks or --regenerate-tokens with API < 34')

        where:
        apiversion = 33
        useIncludeFlags << [true, false]
    }

    def "api version 34 ok for webhooks or webhook regen auth flag"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 34, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'

        if (useIncludeFlags) {
            opts.setIncludeFlags(new HashSet<Archives.ImportFlags>([Archives.ImportFlags.webhooks]))
        } else {
            opts.includeWebhooks = true
        }
        opts.whkRegenAuthTokens = true


        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                _,
                _,
                _,
                _,
                true,
                true,
                _,
                _,
                _,
                _,
                _
        ) >> Calls.response(new ProjectImportStatus(successful: true))
        0 * api._(*_)
        result == 0
        where:
        useIncludeFlags << [true, false]
    }

    def "api version < 38 fails for node sources flag"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, apiversion, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'

        if (useIncludeFlags) {
            opts.setIncludeFlags(new HashSet<Archives.ImportFlags>([Archives.ImportFlags.nodeSources]))
        } else {
            opts.includeNodeSources = true
        }


        when:
        def result = sut.importArchive(opts)

        then:
        InputError exc = thrown()
        exc.message.contains('Cannot use --include-node-sources with API < 38')

        where:
        apiversion = 37
        useIncludeFlags << [true, false]
    }

    def "api version 38 ok for node sources flag"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 38, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'

        if (useIncludeFlags) {
            opts.setIncludeFlags(new HashSet<Archives.ImportFlags>([Archives.ImportFlags.nodeSources]))
        } else {
            opts.includeNodeSources = true
        }

        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                _,
                _,
                _,
                _,
                false,
                false,
                _,
                true,
                _,
                _,
                _
        ) >> Calls.response(new ProjectImportStatus(successful: true))
        0 * api._(*_)
        result == 0
        where:
        useIncludeFlags << [true, false]
    }
    def "api version < 47 fails for webhook regen uuids"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, apiversion, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'

        if (useIncludeFlags) {
            opts.setIncludeFlags(new HashSet<Archives.ImportFlags>([Archives.ImportFlags.webhooks]))
        } else {
            opts.includeWebhooks = true
        }
        opts.whkRegenUuid=true


        when:
        def result = sut.importArchive(opts)

        then:
        InputError exc = thrown()
        exc.message.contains('Cannot use --include-webhooks with --remove-webhooks-uuids with API < 47')

        where:
        apiversion = 46
        useIncludeFlags << [true, false]
    }

    def "api version 47 ok for webhook regen uuids flag"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 47, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'

        if (useIncludeFlags) {
            opts.setIncludeFlags(new HashSet<Archives.ImportFlags>([Archives.ImportFlags.webhooks]))
        } else {
            opts.includeWebhooks = true
        }
        opts.whkRegenUuid=true

        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                _,
                _,
                _,
                _,
                true,
                _,
                true,
                _,
                _,
                _,
                _
        ) >> Calls.response(new ProjectImportStatus(successful: true))
        0 * api._(*_)
        result == 0
        where:
        useIncludeFlags << [true, false]
    }

    def "import include via direct flags"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 38, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'

        opts.noExecutions = !execs
        opts.includeConfig = configs
        opts.includeAcl = acls
        opts.includeScm = scm
        opts.includeWebhooks = webhooks
        opts.includeNodeSources = nodes


        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                execs,
                configs,
                acls,
                scm,
                webhooks,
                _,
                _,
                nodes,
                _,
                _,
                _
        ) >> Calls.response(new ProjectImportStatus(successful: true))
        0 * api._(*_)
        result == 0
        where:
        execs | configs | acls  | scm   | webhooks | nodes
        false | false   | false | false | false    | false
        false | false   | false | false | false    | true
        false | false   | false | false | true     | false
        false | false   | false | true  | false    | false
        false | false   | true  | false | false    | false
        false | true    | false | false | false    | false
        true  | false   | false | false | false    | false
        true  | true    | true  | true  | true     | true
    }

    def "import include via include includeFlags option"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 38, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'

        Set<Archives.ImportFlags> importFlags = new HashSet<>()
        if (execs) {
            importFlags.add(Archives.ImportFlags.executions)
        }
        if (configs) {
            importFlags.add(Archives.ImportFlags.config)
        }
        if (acls) {
            importFlags.add(Archives.ImportFlags.acl)
        }
        if (scm) {
            importFlags.add(Archives.ImportFlags.scm)
        }
        if (webhooks) {
            importFlags.add(Archives.ImportFlags.webhooks)
        }
        if (nodes) {
            importFlags.add(Archives.ImportFlags.nodeSources)
        }
        opts.setIncludeFlags(importFlags)


        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                expectExecs,
                configs,
                acls,
                scm,
                webhooks,
                _,
                _,
                nodes,
                _,
                _,
                _
        ) >> Calls.response(new ProjectImportStatus(successful: true))
        0 * api._(*_)
        result == 0
        where: "include flags determine request params, lack of any params still includes executions"
        execs | expectExecs | configs | acls  | scm   | webhooks | nodes
        false | true        | false   | false | false | false    | false
        false | false       | false   | false | false | false    | true
        false | false       | false   | false | false | true     | false
        false | false       | false   | false | true  | false    | false
        false | false       | false   | true  | false | false    | false
        false | false       | true    | false | false | false    | false
        true  | true        | false   | false | false | false    | false
        true  | true        | true    | true  | true  | true     | true
    }

    def "successful with async import enabled"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.components = ['test-comp'].toSet()
        opts.componentOptions = ['test-comp.key': 'value']
        opts.file = tempFile
        opts.project = 'Aproj'
        opts.asyncImportEnabled = true


        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                true,
                [
                        'importComponents.test-comp': 'true',
                        'importOpts.test-comp.key'  : 'value',
                ],
                _
        ) >> Calls.response(new ProjectImportStatus(successful: true))
        0 * api._(*_)
        result == 0
    }

    def "status endpoint for async import"(){
        given:
        def api = Mock(RundeckApi)
        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 40, true, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)
        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def project = new ProjectNameOptions().with {
            project = "test"
            return it
        }

        when:
        sut.asyncImportStatus(project)

        then:
        1 * api.asyncImportProjectArchiveStatus(project.project) >> Calls.response(new AsyncProjectImportStatus())
        0 * api._(*_)
    }

    def "import some failure has correct exit code"() {

        def api = Mock(RundeckApi)

        def retrofit = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl('http://example.com/fake/').build()
        def out = Mock(CommandOutput)
        def client = new Client(api, retrofit, null, null, 18, true, null)

        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new RdToolImpl(rdapp)

        def sut = new Archives()
        sut.rdOutput = out
        sut.rdTool = rdTool
        def opts = new Archives.ArchiveImportOpts()
        opts.file = tempFile
        opts.project = 'Aproj'
        opts.strict = isstrict


        when:
        def result = sut.importArchive(opts)

        then:
        1 * api.importProjectArchive(
                'Aproj',
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                [:],
                _
        ) >> Calls.response(new ProjectImportStatus(resultsmap))
        0 * api._(*_)
        result == expectExit
        where:
        resultsmap                                     | isstrict | expectExit
        [successful: true]                             | false    | 0
        [successful: true, executionErrors: ['error']] | false    | 0
        [successful: true, executionErrors: ['error']] | true     | 1
        [successful: true, aclErrors: ['error']]       | false    | 0
        [successful: true, aclErrors: ['error']]       | true     | 1
        [successful: false]                            | false    | 1
        [successful: false]                            | true     | 1

    }
}
