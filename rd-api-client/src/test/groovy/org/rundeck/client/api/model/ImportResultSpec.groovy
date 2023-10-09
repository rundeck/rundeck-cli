package org.rundeck.client.api.model

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import spock.lang.Specification

import java.lang.annotation.Annotation

class ImportResultSpec extends Specification {
    def "parse import result json"() {
        given:
            def text='''{
"succeeded": [],
"failed": [
    {
        "index":1,
        "name":"test00",
        "group":"group00",
        "project":"bongo",
        "error":"Cannot create a Job with UUID 17b07de4-f47d-4ab1-9f3a-8d47b33058c4: a Job already exists..."
    },
    {
        "index":2,
        "name":"test01",
        "group":"",
        "project":"bongo",
        "error":"...errr"
    }
],
"skipped":[
    {
        "index":12,
        "name":"test02",
        "group":"group02",
        "project":"bongo",
        "error":"...errr"
    },
    {
        "index":13,
        "name":"test03",
        "group":"",
        "project":"bongo",
        "error":"...errr"
    }
]
}
'''
            def retrofit = new Retrofit.Builder().baseUrl('http://test').
                addConverterFactory(JacksonConverterFactory.create()).
                build()

        when:
            Converter<ResponseBody, ImportResult> converter = retrofit.
                responseBodyConverter(ImportResult.class, [] as Annotation[],);
            ImportResult result = converter.convert(ResponseBody.create(text, MediaType.parse('application/json')))

        then:
            result.succeeded!=null
            result.succeeded.size()==0
            result.failed!=null
            result.failed.size()==2
            result.failed[0].name=='test00'
            result.failed[0].group=='group00'
            result.failed[0].project=='bongo'
            result.failed[0].error=='Cannot create a Job with UUID 17b07de4-f47d-4ab1-9f3a-8d47b33058c4: a Job already exists...'
            result.failed[1].name=='test01'
            result.failed[1].group==''
            result.failed[1].project=='bongo'
            result.failed[1].error=='...errr'
            result.skipped!=null
            result.skipped.size()==2
            result.skipped[0].name=='test02'
            result.skipped[0].group=='group02'
            result.skipped[0].project=='bongo'
            result.skipped[0].error=='...errr'
            result.skipped[1].name=='test03'
            result.skipped[1].group==''
            result.skipped[1].project=='bongo'
            result.skipped[1].error=='...errr'
    }
}
