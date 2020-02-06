package org.rundeck.client.api.model

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import spock.lang.Specification

import java.lang.annotation.Annotation

class JobLoadItemSpec extends Specification {
    def "parse job load item with empty job name"() {
        given:
            def xmlText = '''
    <job index='1' href='http://rundeck.local:4440/rundeck/api/34/job/896a3c9e-f765-43cc-8e1a-15566fe275fa'>
      <id>896a3c9e-f765-43cc-8e1a-15566fe275fa</id>
      <permalink>http://rundeck.local:4440/rundeck/project/asdf/job/show/896a3c9e-f765-43cc-8e1a-15566fe275fa</permalink>
      <name></name>
      <group>Sales</group>
      <project>asdf</project>
      <error>Job Name is required</error>
    </job>'''
            def retrofit = new Retrofit.Builder().baseUrl('http://test').
                    addConverterFactory(SimpleXmlConverterFactory.create()).
                    build()

        when:
            Converter<ResponseBody, JobLoadItem> converter = retrofit.
                    responseBodyConverter(JobLoadItem.class, [] as Annotation[],);
            JobLoadItem result = converter.convert(ResponseBody.create(MediaType.parse('application/xml'), xmlText))

        then:
            result != null
            result.name == null
            result.id == '896a3c9e-f765-43cc-8e1a-15566fe275fa'
            result.permalink == 'http://rundeck.local:4440/rundeck/project/asdf/job/show/896a3c9e-f765-43cc-8e1a-15566fe275fa'
            result.group == 'Sales'
            result.project == 'asdf'
            result.error == 'Job Name is required'


    }
}
