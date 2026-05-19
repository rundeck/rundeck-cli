package org.rundeck.client.util

import okhttp3.*
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for {@link CrossOriginRedirectInterceptor}.
 */
class CrossOriginRedirectInterceptorSpec extends Specification {

    private static Request buildRequest(String url) {
        new Request.Builder().url(url).build()
    }

    private static Response buildResponse(Request request, int code, String location = null) {
        def builder = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message('test')
                .body(ResponseBody.create(MediaType.parse('text/plain'), ''))
        if (location != null) {
            builder.header('Location', location)
        }
        builder.build()
    }

    def "cross-origin redirect to different host is blocked by default"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 302, 'http://attacker.example.com/capture')

        when:
        interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        def e = thrown(IOException)
        e.message.contains('Cross-origin redirect blocked')
        e.message.contains('attacker.example.com')
    }

    def "cross-origin redirect to different scheme is blocked by default"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 301, 'https://myserver.example.com/api/48/system/info')

        when:
        interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        thrown(IOException)
    }

    def "cross-origin redirect to different port is blocked by default"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com:4440/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 307, 'http://myserver.example.com:19081/capture')

        when:
        interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        thrown(IOException)
    }

    def "same-origin redirect is passed through without error"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 302, 'http://myserver.example.com/api/48/login')

        when:
        def result = interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        result == response
        noExceptionThrown()
    }

    def "cross-origin redirect is allowed with opt-in enabled"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(true)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 302, 'http://attacker.example.com/capture')

        when:
        def result = interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        result == response
        noExceptionThrown()
    }

    def "2xx response is passed through unchanged"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 200)

        when:
        def result = interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        result == response
        noExceptionThrown()
    }

    def "4xx response is passed through unchanged"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 404)

        when:
        def result = interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        result == response
        noExceptionThrown()
    }

    def "5xx response is passed through unchanged"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 500)

        when:
        def result = interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        result == response
        noExceptionThrown()
    }

    def "error message includes the blocked redirect target URL"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def blockedUrl = 'http://evil.com/steal-creds'
        def response = buildResponse(request, 302, blockedUrl)

        when:
        interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        def e = thrown(IOException)
        e.message.contains(blockedUrl)
    }

    def "error message hints at RD_ALLOW_CROSS_ORIGIN_REDIRECT opt-in"() {
        given:
        def interceptor = new CrossOriginRedirectInterceptor(false)
        def request = buildRequest('http://myserver.example.com/api/48/system/info')
        def chain = Mock(Interceptor.Chain)
        def response = buildResponse(request, 302, 'http://other.com/path')

        when:
        interceptor.intercept(chain)

        then:
        _ * chain.request() >> request
        1 * chain.proceed(request) >> response
        def e = thrown(IOException)
        e.message.contains('RD_ALLOW_CROSS_ORIGIN_REDIRECT')
    }

    @Unroll
    def "isSameOrigin: #url1 vs #url2 => #expected"() {
        expect:
        CrossOriginRedirectInterceptor.isSameOrigin(
                HttpUrl.parse(url1),
                HttpUrl.parse(url2)
        ) == expected

        where:
        url1                                   | url2                                    | expected
        'http://host/path1'                    | 'http://host/path2'                     | true
        'http://host:4440/path1'               | 'http://host:4440/path2'                | true
        'http://host/path'                     | 'https://host/path'                     | false
        'http://host:4440/path'                | 'http://host:4441/path'                 | false
        'http://host1/path'                    | 'http://host2/path'                     | false
        'http://myserver.example.com:4440/api' | 'http://myserver.example.com/api'       | false
    }
}
