package org.rundeck.client.api.model

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jaxb.JaxbConverterFactory
import spock.lang.Specification

import java.lang.annotation.Annotation

class ImportResultSpec extends Specification {
    def "parse import result xml"(){
        given:
            def xmlText='''<result success='true' apiversion='35'>
  <succeeded count='0' />
  <failed count='2'>
    <job index='1'>
      <name>test00</name>
      <group>group00</group>
      <project>bongo</project>
      <error>Cannot create a Job with UUID 17b07de4-f47d-4ab1-9f3a-8d47b33058c4: a Job already exists...</error>
    </job>
    <job index='2'>
      <name>test01</name>
      <group></group>
      <project>bongo</project>
      <error>...errr</error>
    </job>
  </failed>
  <skipped count='2'>
    <job index='12'>
      <name>test02</name>
      <group>group02</group>
      <project>bongo</project>
      
      <error>...errr</error>
    </job>
    <job index='13'>
      <name>test03</name>
      <group></group>
      <project>bongo</project>
      <error>...errr</error>
    </job>
  </skipped> 
</result>
'''
            def retrofit = new Retrofit.Builder().baseUrl('http://test').
                addConverterFactory(JaxbConverterFactory.create()).
                build()

        when:
            Converter<ResponseBody, ImportResult> converter = retrofit.
                responseBodyConverter(ImportResult.class, [] as Annotation[],);
            ImportResult result = converter.convert(ResponseBody.create(MediaType.parse('application/xml'), xmlText))

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
