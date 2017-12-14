package org.rundeck.client.api.model

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.annotation.Annotation

/**
 * @author greg
 * @since 12/13/17
 */
class ErrorResponseSpec extends Specification {
    @Unroll
    def "xml parse code not required"() {
        given:
        def retrofit = new Retrofit.Builder().baseUrl('http://test').
                addConverterFactory(SimpleXmlConverterFactory.create()).
                build()

        when:
        Converter<ResponseBody, ErrorResponse> converter = retrofit.responseBodyConverter(
                ErrorResponse.class,
                [] as Annotation[],
                );
        ErrorResponse result = converter.convert(ResponseBody.create(MediaType.parse('application/xml'), xmlText))

        then:

        result != null
        result.errorCode == code
        result.error == error
        result.message == message
        result.messages == null
        result.errorMessage == message
        result.apiVersion == version

        where:
        xmlText                                                                                                 |
                code  |
                error  |
                message | version
        '<result error=\'true\' apiversion=\'20\'><error><message>blah</message></error></result>'              |
                null  |
                'true' |
                'blah'  | 20
        '<result error=\'true\' apiversion=\'20\'><error code=\'xyz\'><message>blah</message></error></result>' |
                'xyz' |
                'true' |
                'blah'  | 20

    }

    @Unroll
    def "xml parse multi message"() {
        given:
        def retrofit = new Retrofit.Builder().baseUrl('http://test').
                addConverterFactory(SimpleXmlConverterFactory.create()).
                build()

        when:
        Converter<ResponseBody, ErrorResponse> converter = retrofit.responseBodyConverter(
                ErrorResponse.class,
                [] as Annotation[],
                );
        ErrorResponse result = converter.convert(ResponseBody.create(MediaType.parse('application/xml'), xmlText))

        then:

        result != null
        result.errorCode == code
        result.error == error
        result.messages == messages
        result.errorMessage == messages.toString()
        result.apiVersion == version

        where:
        xmlText                                                                                                      |
                code  |
                error  |
                messages          |
                version
        '<result error=\'true\' apiversion=\'20\'><error ' +
                'code=\'xyz\'><messages><message>blah</message><message>blah2</message></messages></error></result>' |
                'xyz' |
                'true' |
                ['blah', 'blah2'] |
                20

    }

    def "json parse with multiple messages"() {
        given:
        def retrofit = new Retrofit.Builder().baseUrl('http://test').
                addConverterFactory(JacksonConverterFactory.create()).
                build()

        when:
        Converter<ResponseBody, ErrorResponse> converter = retrofit.responseBodyConverter(
                ErrorResponse.class,
                [] as Annotation[],
                );
        ErrorResponse result = converter.convert(
                ResponseBody.create(MediaType.parse('application/json'), jsonText)
        )

        then:

        result != null
        result.errorCode == code
        result.error == error
        result.message == message
        result.messages == messages
        result.apiVersion == version

        where:
        jsonText                                                                               |
                code                |
                error  |
                message     |
                messages   |
                version
        '{"error":true,"apiversion":21,"errorCode":"test","messages":["a","b"]}'               |
                'test'              |
                'true' |
                null        |
                ['a', 'b'] |
                21
        '{"error":true,"apiversion":21,"errorCode":"api.error.unknown","message":"A message"}' |
                'api.error.unknown' |
                'true' |
                'A message' |
                null       |
                21

    }
}
