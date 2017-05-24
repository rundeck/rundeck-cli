package org.rundeck.client.util

import okhttp3.*
import spock.lang.Specification

/**
 * @author greg
 * @since 5/24/17
 */
class FormAuthInterceptorSpec extends Specification {
    def "form auth without cookie"() {
        given:


        String user = 'auser'
        String pass = 'apass'
        String baseurl = 'http://host/base/path'
        String securityurl = 'http://host/base/path/j_security_path'
        String userfield = 'j_username'
        String passwordfield = 'j_password'
        String errorpath = '/login/error'
        def sut = new FormAuthInterceptor(
                user,
                pass,
                baseurl,
                securityurl,
                userfield,
                passwordfield,
                errorpath
        )

        def firstrequest = new Request.Builder().url(starturl).build()
        def chain = Mock(Interceptor.Chain)

        def okresponse = new Response.Builder().with {
            request firstrequest
            protocol Protocol.HTTP_1_1
            code 200
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }


        when:
        def response = sut.intercept(chain)


        then:
        1 * chain.proceed({ req -> req.url().toString() == baseurl }) >> okresponse
        1 * chain.proceed({ req -> req.url().toString() == securityurl }) >> okresponse
        1 * chain.proceed({ req -> req.url().toString() == starturl }) >> okresponse
        _ * chain.request() >> firstrequest
        0 * chain._(*_)
        response == okresponse


        where:
        starturl                    | _
        'http://host/path/api/blah' | _
    }

    def "form auth without cookie then with cookie"() {
        given:

        String user = 'auser'
        String pass = 'apass'
        String baseurl = 'http://host/base/path'
        String securityurl = 'http://host/base/path/j_security_path'
        String userfield = 'j_username'
        String passwordfield = 'j_password'
        String errorpath = '/login/error'
        def sut = new FormAuthInterceptor(
                user,
                pass,
                baseurl,
                securityurl,
                userfield,
                passwordfield,
                errorpath
        )

        def firstrequest = new Request.Builder().url(starturl).build()
        def chain = Mock(Interceptor.Chain)
        def secondrequest = new Request.Builder().url(secondurl).build()
        def chain2 = Mock(Interceptor.Chain)

        def okresponse = new Response.Builder().with {
            request firstrequest
            protocol Protocol.HTTP_1_1
            code 200
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }
        def firstresponse = new Response.Builder().with {
            request firstrequest
            protocol Protocol.HTTP_1_1
            code 200
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }
        def secondresponse = new Response.Builder().with {
            request secondrequest
            protocol Protocol.HTTP_1_1
            code 200
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }

        when:
        def response = sut.intercept(chain)
        def response2 = sut.intercept(chain2)


        then:
        1 * chain.proceed({ req -> req.url().toString() == baseurl }) >> okresponse
        1 * chain.proceed({ req -> req.url().toString() == securityurl }) >> okresponse
        1 * chain.proceed(firstrequest) >> firstresponse
        _ * chain.request() >> firstrequest
        0 * chain._(*_)

        _ * chain2.request() >> secondrequest
        1 * chain2.proceed(secondrequest) >> secondresponse
        0 * chain2._(*_)

        response == firstresponse
        response2 == secondresponse


        where:
        starturl                    | secondurl
        'http://host/path/api/blah' | 'http://host/path/api/blah2'
    }

    def "form auth without cookie security response not successful"() {
        given:


        String user = 'auser'
        String pass = 'apass'
        String baseurl = 'http://host/base/path'
        String securityurl = 'http://host/base/path/j_security_path'
        String userfield = 'j_username'
        String passwordfield = 'j_password'
        String errorpath = '/login/error'
        def sut = new FormAuthInterceptor(
                user,
                pass,
                baseurl,
                securityurl,
                userfield,
                passwordfield,
                errorpath
        )

        def firstrequest = new Request.Builder().url(starturl).build()
        def chain = Mock(Interceptor.Chain)

        def okresponse = new Response.Builder().with {
            request firstrequest
            protocol Protocol.HTTP_1_1
            code 200
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }

        def errorResponse = new Response.Builder().with {
            request firstrequest
            protocol Protocol.HTTP_1_1
            code 400
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }



        when:
        def response = sut.intercept(chain)


        then:
        1 * chain.proceed({ req -> req.url().toString() == baseurl }) >> okresponse
        1 * chain.proceed({ req -> req.url().toString() == securityurl }) >> errorResponse
        _ * chain.request() >> firstrequest
        0 * chain._(*_)
        IllegalStateException e = thrown()
        e.message =~ /Password Authentication failed, expected a successful response./

        where:
        starturl                    | _
        'http://host/path/api/blah' | _
    }

    def "form auth without cookie security response bad password"() {
        given:


        String user = 'auser'
        String pass = 'apass'
        String baseurl = 'http://host/base/path'
        String securityurl = 'http://host/base/path/j_security_path'
        String userfield = 'j_username'
        String passwordfield = 'j_password'
        String errorpath = '/login/error'
        def sut = new FormAuthInterceptor(
                user,
                pass,
                baseurl,
                securityurl,
                userfield,
                passwordfield,
                errorpath
        )

        def firstrequest = new Request.Builder().url(starturl).build()
        def chain = Mock(Interceptor.Chain)

        def okresponse = new Response.Builder().with {
            request firstrequest
            protocol Protocol.HTTP_1_1
            code 200
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }

        def loginErrorResponse = new Response.Builder().with {
            request new Request.Builder().url(baseurl + errorpath).build()
            protocol Protocol.HTTP_1_1
            code 200
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }



        when:
        def response = sut.intercept(chain)


        then:
        1 * chain.proceed({ req -> req.url().toString() == baseurl }) >> okresponse
        1 * chain.proceed({ req -> req.url().toString() == securityurl }) >> loginErrorResponse
        _ * chain.request() >> firstrequest
        0 * chain._(*_)
        IllegalStateException e = thrown()
        e.message =~ /Password Authentication failed, expected a successful response./

        where:
        starturl                    | _
        'http://host/path/api/blah' | _
    }

    def "form auth base url not successful"() {
        given:


        String user = 'auser'
        String pass = 'apass'
        String baseurl = 'http://host/base/path'
        String securityurl = 'http://host/base/path/j_security_path'
        String userfield = 'j_username'
        String passwordfield = 'j_password'
        String errorpath = '/login/error'
        def sut = new FormAuthInterceptor(
                user,
                pass,
                baseurl,
                securityurl,
                userfield,
                passwordfield,
                errorpath
        )

        def firstrequest = new Request.Builder().url(starturl).build()
        def chain = Mock(Interceptor.Chain)

        def errorResponse = new Response.Builder().with {
            request firstrequest
            protocol Protocol.HTTP_1_1
            code 400
            body ResponseBody.create(MediaType.parse('text/html'), 'blah')
            build()
        }


        when:
        def response = sut.intercept(chain)


        then:
        1 * chain.proceed({ req -> req.url().toString() == baseurl }) >> errorResponse
        _ * chain.request() >> firstrequest
        0 * chain._(*_)

        IllegalStateException e = thrown()
        e.message =~ /Expected successful response from: $baseurl/


        where:
        starturl                    | _
        'http://host/path/api/blah' | _
    }
}
