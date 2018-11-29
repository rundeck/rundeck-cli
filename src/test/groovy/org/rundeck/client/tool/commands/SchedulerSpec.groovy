/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.tool.commands

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.api.model.*
import org.rundeck.client.api.model.scheduler.SchedulerTakeover
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import org.rundeck.toolbelt.CommandOutput
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.mock.Calls
import spock.lang.Specification

/**
 * @author greg
 * @since 12/5/16
 */
class SchedulerSpec extends Specification {

    def "parse takeover"() {
        given:
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody('''{
  "takeoverSchedule": {
    "jobs": {
      "failed": [
        {
          "href": "http://dignan:4440/api/14/job/a1aa53ac-73a6-4ead-bbe4-34afbff8e057",
          "permalink": "http://dignan:4440/job/show/a1aa53ac-73a6-4ead-bbe4-34afbff8e057",
          "id": "11111111-73a6-4ead-1111-34afbff8e057",
          "previous-owner": "8F3D5976-2232-4529-847B-8E45764608E3"
        },
        {
          "href": "http://dignan:4440/api/14/job/116e2025-7895-444a-88f7-d96b4f19fdb3",
          "permalink": "http://dignan:4440/job/show/116e2025-7895-444a-88f7-d96b4f19fdb3",
          "id": "116e2025-7895-444a-88f7-d96b4f19fdb3",
          "previous-owner": "8F3D5976-2232-4529-847B-8E45764608E3"
        }
      ],
      "successful": [
        {
          "href": "http://dignan:4440/api/14/job/a1aa53ac-73a6-4ead-bbe4-34afbff8e057",
          "permalink": "http://dignan:4440/job/show/a1aa53ac-73a6-4ead-bbe4-34afbff8e057",
          "id": "a1aa53ac-73a6-4ead-bbe4-34afbff8e057",
          "previous-owner": "8F3D5976-2232-4529-847B-8E45764608E3"
        },
        {
          "href": "http://dignan:4440/api/14/job/116e2025-7895-444a-88f7-d96b4f19fdb3",
          "permalink": "http://dignan:4440/job/show/116e2025-7895-444a-88f7-d96b4f19fdb3",
          "id": "116e2025-7895-444a-88f7-d96b4f19fdb3",
          "previous-owner": "8F3D5976-2232-4529-847B-8E45764608E3"
        }
      ],
      "total": 4
    },
    "server": {
      "uuid": "8F3D5976-2232-4529-847B-8E45764608E3"
    }
  },
  "self": {
    "server": {
      "uuid": "C677C663-F902-4B97-B8AC-4AA57B58DDD6"
    }
  },
  "message": "Schedule Takeover successful for 2/2 Jobs.",
  "apiversion": 14,
  "success": true
}'''
        ).addHeader('content-type', 'application/json')
        );
        server.start()

        def retrofit = new Retrofit.Builder().baseUrl(server.url('/api/14/')).
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        def api = retrofit.create(RundeckApi)

        when:
        def body = api.takeoverSchedule(new SchedulerTakeover()).execute().body()

        then:
        RecordedRequest request1 = server.takeRequest()
        request1.path == '/api/14/scheduler/takeover'


        body.apiversion == 14
        body.success == true
        body.takeoverSchedule != null
        body.takeoverSchedule.jobs != null
        body.takeoverSchedule.jobs.total == 4
        body.takeoverSchedule.jobs.successful.size() == 2
        body.takeoverSchedule.jobs.failed.size() == 2
        body.takeoverSchedule.jobs.successful.get(0).id == "a1aa53ac-73a6-4ead-bbe4-34afbff8e057"
        body.takeoverSchedule.jobs.failed.get(0).id == "11111111-73a6-4ead-1111-34afbff8e057"
        body.takeoverSchedule.jobs.failed.get(0).previousOwner == "8F3D5976-2232-4529-847B-8E45764608E3"
        server.shutdown()

    }

}
