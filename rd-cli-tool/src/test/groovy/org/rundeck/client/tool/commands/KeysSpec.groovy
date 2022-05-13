package org.rundeck.client.tool.commands

import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.InputError


import okhttp3.RequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.KeyStorageItem
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author greg
 * @since 5/4/17
 */
class KeysSpec extends Specification {
    @Unroll
    def "parse key upload file default charset"() {
        given:
        File testfile = File.createTempFile('KeysSpec', '.test')
        testfile.text = input

        def opts = Mock(Keys.Upload) {
            getPath() >> new Keys.Path('keys/test1')
            getType() >> KeyStorageItem.KeyFileType.password
            getFile() >> testfile
            isFile() >> true
        }
        when:
        def body = Keys.prepareKeyUpload(opts)


        then:
        Buffer buffer = new Buffer()
        body.writeTo(buffer)
        buffer.readByteArray() == expectstr.bytes

        where:
        input        | expectstr
        'asdf'       | 'asdf'
        'asdf\n'     | 'asdf'
        'asdf\r\n'   | 'asdf'
        'asdf😀\r\n' | 'asdf😀'
    }

    @Unroll
    def "parse key upload file with charset"() {
        given:
        File testfile = File.createTempFile('KeysSpec', '.test')
        testfile.setText(input, charset)

        def opts = Mock(Keys.Upload) {
            getPath() >> new Keys.Path('keys/test1')
            getType() >> KeyStorageItem.KeyFileType.password
            getFile() >> testfile
            isFile() >> true
            getCharset() >> charset
            isCharset() >> true
        }
        when:
        def body = Keys.prepareKeyUpload(opts)


        then:
        Buffer buffer = new Buffer()
        body.writeTo(buffer)
        buffer.readByteArray() == expectstr.bytes

        where:
        input        | expectstr | charset
        'asdf😀\r\n' | 'asdf😀'  | 'UTF-8'
        'asdf😀\r\n' | 'asdf😀'  | 'UTF-16'
    }

    @Unroll
    def "parse key upload invalid"() {
        given:
        File testfile = File.createTempFile('KeysSpec', '.test')
        testfile.text = input

        def opts = Mock(Keys.Upload) {
            getPath() >> new Keys.Path('keys/test1')
            getType() >> KeyStorageItem.KeyFileType.password
            getFile() >> testfile
            isFile() >> true
        }
        when:
        def body = Keys.prepareKeyUpload(opts)


        then:
        IllegalStateException e = thrown()

        where:
        input  | _
        ''     | _
        '\n'   | _
        '\r\n' | _
    }

    @Unroll
    def "create password require file or prompt"() {
        given:

        def opts = Mock(Keys.Upload) {
            getPath() >> new Keys.Path('keys/test1')
            getType() >> type
            isFile() >> false
            isPrompt() >> false
        }
        when:
        def body = Keys.prepareKeyUpload(opts)

        then:
        InputError e = thrown()
        e.message == 'File (-f/--file) or -P/--prompt is required for type: password'

        where:
        type                                | file | prompt
        KeyStorageItem.KeyFileType.password | null | false
    }

    @Unroll
    def "create #type require file"() {
        given:

        def opts = Mock(Keys.Upload) {
            getPath() >> new Keys.Path('keys/test1')
            getType() >> type
            isFile() >> false
            isPrompt() >> prompt
        }
        when:
        def body = Keys.prepareKeyUpload(opts)

        then:
        InputError e = thrown()
        e.message =~ /File \(-f\/--file\) is required for type:/

        where:
        type                                  | prompt
        KeyStorageItem.KeyFileType.privateKey | false
        KeyStorageItem.KeyFileType.privateKey | true
        KeyStorageItem.KeyFileType.publicKey  | false
        KeyStorageItem.KeyFileType.publicKey  | true
    }

    private RdTool setupMock(RundeckApi api, int apiVersion=18) {
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, apiVersion, true, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new MockRdTool(client: client, rdApp: rdapp)
        rdTool.appConfig = Mock(RdClientConfig)
        rdTool
    }

    @Unroll
    def "create password from file"() {
        given:
        File testfile = File.createTempFile('KeysSpec', '.test')
        testfile.text = input
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Keys command = new Keys()
        command.rdTool=rdTool
        command.rdOutput=out

        def opts = new Keys.Upload()
        opts.type= KeyStorageItem.KeyFileType.password
        opts.file= testfile
        opts.path= new Keys.Path('keys/test1')
        when:
        command.create(opts)

        then:
        1 * api.createKeyStorage('test1', {
            RequestBody body = it
            Buffer buffer = new Buffer()
            body.writeTo(buffer)
            buffer.readByteArray() == expectstr.bytes
        }
        ) >> Calls.response(new KeyStorageItem())
        0 * api._(*_)

        where:
        input        | length | expectstr
        'asdf'       | 4      | 'asdf'
        'asdf\n'     | 4      | 'asdf'
        'asdf\r\n'   | 4      | 'asdf'
        'asdf😀\r\n' | 5      | 'asdf😀'

    }

    @Unroll
    def "create password from file2"() {
        given:
        File testfile = File.createTempFile('KeysSpec', '.test')
        testfile.text = input
        MockWebServer server = new MockWebServer()
        server.enqueue(
                new MockResponse().
                        setBody('''{
                  "path":"keys",
                  "type":"file",
                  "name":"test1",
                  "url":"",
                  "meta":{"a":"b"}
                  
                }'''
                        ).
                        addHeader('content-type', 'application/json')
        )
        server.start()

        def retrofit = new Retrofit.Builder().baseUrl(server.url('/api/18/')).
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        def api = retrofit.create(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Keys command = new Keys()
        command.rdTool=rdTool
        command.rdOutput=out

        def opts = new Keys.Upload()
        opts.type= KeyStorageItem.KeyFileType.password
        opts.file= testfile
        opts.path= new Keys.Path('keys/test1')

        when:
        command.create(opts)

        then:
        RecordedRequest request1 = server.takeRequest()
        request1.path == '/api/18/storage/keys/test1'
        request1.method == 'POST'
        request1.getHeader('Content-Type') == 'application/x-rundeck-data-password'
//        request1.body.size() == length
        Buffer buffer = new Buffer()
        def baos = new ByteArrayOutputStream()
        request1.body.writeTo(baos)
        baos.toByteArray() == expectstr.bytes


        where:
        input        | expectstr
        'asdf'       | 'asdf'
        '1234567890' | '1234567890'
        'asdf\n'     | 'asdf'
        'asdf\r\n'   | 'asdf'
        'asdf😀\r\n' | 'asdf😀'

    }

    @Unroll
    def "list all"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api)
        def out = Mock(CommandOutput)
        Keys command = new Keys()
        command.rdTool=rdTool
        command.rdOutput=out
        def opts = new Keys.OptionalPath()
        opts.path= new Keys.Path(input?:"")


        when:
        command.list(opts)

        then:
        1 * api.listKeyStorage(_) >> Calls.response(new KeyStorageItem())
        0 * api._(*_)

        where:
        input       | _
        null        | _
        'keys/'     | _
    }
}
