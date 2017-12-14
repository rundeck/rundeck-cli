package org.rundeck.client.api.model

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import spock.lang.Specification

import java.lang.annotation.Annotation

/**
 * @author greg
 * @since 12/13/17
 */
class ErrorResponseSpec extends Specification {
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
}
