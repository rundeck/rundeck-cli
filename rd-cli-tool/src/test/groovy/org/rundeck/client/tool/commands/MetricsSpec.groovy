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
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import spock.lang.Specification

/**
 * @author ahormazabal
 * @since 2018/11/26
 */
class MetricsSpec extends Specification {

    def "parse metrics data deserialization"() {
        given:
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody('''{
  "version": "3.1.3",
  "gauges": {
    "dataSource.connection.pingTime": {
      "value": 1
    },
    "rundeck.scheduler.quartz.runningExecutions": {
      "value": 0
    },
    "rundeck.services.AuthorizationService.sourceCache.evictionCount": {
      "value": 0
    },
    "rundeck.services.AuthorizationService.sourceCache.hitCount": {
      "value": 9
    },
    "rundeck.services.AuthorizationService.sourceCache.hitRate": {
      "value": 0.9
    },
    "rundeck.services.AuthorizationService.sourceCache.loadExceptionCount": {
      "value": 0
    },
    "rundeck.services.AuthorizationService.sourceCache.missCount": {
      "value": 1
    },
    "rundeck.services.NodeService.nodeCache.evictionCount": {
      "value": 0
    },
    "rundeck.services.NodeService.nodeCache.hitCount": {
      "value": 11
    },
    "rundeck.services.NodeService.nodeCache.hitRate": {
      "value": 0.9166666666666666
    },
    "rundeck.services.NodeService.nodeCache.loadExceptionCount": {
      "value": 0
    },
    "rundeck.services.NodeService.nodeCache.missCount": {
      "value": 1
    },
    "rundeck.services.ProjectManagerService.fileCache.evictionCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.fileCache.hitCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.fileCache.hitRate": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.fileCache.loadExceptionCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.fileCache.missCount": {
      "value": 6
    },
    "rundeck.services.ProjectManagerService.projectCache.evictionCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.projectCache.hitCount": {
      "value": 530
    },
    "rundeck.services.ProjectManagerService.projectCache.hitRate": {
      "value": 0.9888059701492538
    },
    "rundeck.services.ProjectManagerService.projectCache.loadExceptionCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.projectCache.missCount": {
      "value": 6
    },
    "rundeck.services.ProjectManagerService.sourceCache.evictionCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.sourceCache.hitCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.sourceCache.hitRate": {
      "value": 1
    },
    "rundeck.services.ProjectManagerService.sourceCache.loadExceptionCount": {
      "value": 0
    },
    "rundeck.services.ProjectManagerService.sourceCache.missCount": {
      "value": 0
    }
  },
  "counters": {
    "rundeck.scheduler.quartz.scheduledJobs": {
      "count": 6
    }
  },
  "histograms": {},
  "meters": {
    "rundeck.services.AuthorizationService.systemAuthorization.evaluateMeter": {
      "count": 4,
      "m15_rate": 0.00314076610191179,
      "m1_rate": 0.023875601445527157,
      "m5_rate": 0.008400048550624787,
      "mean_rate": 0.026528128813374685,
      "units": "events/second"
    },
    "rundeck.services.AuthorizationService.systemAuthorization.evaluateSetMeter": {
      "count": 12,
      "m15_rate": 0.6892782713428792,
      "m1_rate": 0.1267495745218626,
      "m5_rate": 0.5153224625291539,
      "mean_rate": 0.07958452971073966,
      "units": "events/second"
    },
    "rundeck.services.ExecutionService.executionJobStartMeter": {
      "count": 6,
      "m15_rate": 0.3446391356714396,
      "m1_rate": 0.0633747872609313,
      "m5_rate": 0.25766123126457696,
      "mean_rate": 0.039926086575635206,
      "units": "events/second"
    },
    "rundeck.services.ExecutionService.executionStartMeter": {
      "count": 6,
      "m15_rate": 0.3446391356714396,
      "m1_rate": 0.0633747872609313,
      "m5_rate": 0.25766123126457696,
      "mean_rate": 0.039893916635731115,
      "units": "events/second"
    },
    "rundeck.services.ExecutionService.executionSuccessMeter": {
      "count": 6,
      "m15_rate": 0.3465591259042392,
      "m1_rate": 0.06888231291145262,
      "m5_rate": 0.2619915710449402,
      "mean_rate": 0.04041785210623091,
      "units": "events/second"
    }
  },
  "timers": {
    "rundeck.api.requests.requestTimer": {
      "count": 3,
      "max": 0.19907502000000002,
      "mean": 0.108295424,
      "min": 0.014703712,
      "p50": 0.11110754,
      "p75": 0.19907502000000002,
      "p95": 0.19907502000000002,
      "p98": 0.19907502000000002,
      "p99": 0.19907502000000002,
      "p999": 0.19907502000000002,
      "stddev": 0.09221781715431031,
      "m15_rate": 0.00314076610191179,
      "m1_rate": 0.023875601445527157,
      "m5_rate": 0.008400048550624787,
      "mean_rate": 0.0326409948656659,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.quartzjobs.ExecutionJob.executionTimer": {
      "count": 6,
      "max": 2.785892703,
      "mean": 0.6245617408333334,
      "min": 0.156943942,
      "p50": 0.2009052745,
      "p75": 0.8750149552500001,
      "p95": 2.785892703,
      "p98": 2.785892703,
      "p99": 2.785892703,
      "p999": 2.785892703,
      "stddev": 1.0593646577233937,
      "m15_rate": 0.17537122122178622,
      "m1_rate": 0.049487919338571586,
      "m5_rate": 0.13657375399032906,
      "mean_rate": 0.039744906881515114,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.services.AuthorizationService.getSystemAuthorization": {
      "count": 10,
      "max": 0.11291242300000001,
      "mean": 0.0168355508,
      "min": 0.002907568,
      "p50": 0.003709257,
      "p75": 0.0103103045,
      "p95": 0.11291242300000001,
      "p98": 0.11291242300000001,
      "p99": 0.11291242300000001,
      "p999": 0.11291242300000001,
      "stddev": 0.0345229796390412,
      "m15_rate": 0.3477799017733514,
      "m1_rate": 0.08725038870645847,
      "m5_rate": 0.2660612798152018,
      "mean_rate": 0.06628145573313499,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.services.AuthorizationService.systemAuthorization.evaluateSetTimer": {
      "count": 12,
      "max": 0.064324482,
      "mean": 0.0064664489166666676,
      "min": 0.0006582410000000001,
      "p50": 0.001099722,
      "p75": 0.0018917270000000002,
      "p95": 0.064324482,
      "p98": 0.064324482,
      "p99": 0.064324482,
      "p999": 0.064324482,
      "stddev": 0.01822988971428955,
      "m15_rate": 0.6892782713428792,
      "m1_rate": 0.1267495745218626,
      "m5_rate": 0.5153224625291539,
      "mean_rate": 0.07958252469001104,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.services.AuthorizationService.systemAuthorization.evaluateTimer": {
      "count": 4,
      "max": 0.002291996,
      "mean": 0.00132195675,
      "min": 0.000826429,
      "p50": 0.001084701,
      "p75": 0.00204558875,
      "p95": 0.002291996,
      "p98": 0.002291996,
      "p99": 0.002291996,
      "p999": 0.002291996,
      "stddev": 0.0006824895873415091,
      "m15_rate": 0.00314076610191179,
      "m1_rate": 0.023875601445527157,
      "m5_rate": 0.008400048550624787,
      "mean_rate": 0.0265274537259422,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.services.FrameworkService.authorizeApplicationResource": {
      "count": 4,
      "max": 0.016328387,
      "mean": 0.004882139000000001,
      "min": 0.000912978,
      "p50": 0.0011435955,
      "p75": 0.012589778000000001,
      "p95": 0.016328387,
      "p98": 0.016328387,
      "p99": 0.016328387,
      "p999": 0.016328387,
      "stddev": 0.007633923731977984,
      "m15_rate": 0.3711760292127013,
      "m1_rate": 0.14055240663997448,
      "m5_rate": 0.32006153577038915,
      "mean_rate": 0.05025453574530793,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.services.FrameworkService.filterNodeSet": {
      "count": 12,
      "max": 0.36586338300000004,
      "mean": 0.03359687208333334,
      "min": 0.000560871,
      "p50": 0.0010942600000000001,
      "p75": 0.00812147625,
      "p95": 0.36586338300000004,
      "p98": 0.36586338300000004,
      "p99": 0.36586338300000004,
      "p999": 0.36586338300000004,
      "stddev": 0.10477591919675994,
      "m15_rate": 0.6892782713428792,
      "m1_rate": 0.1267495745218626,
      "m5_rate": 0.5153224625291539,
      "mean_rate": 0.07994704576896616,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.services.NodeService.project.bingo.loadNodes": {
      "count": 3,
      "max": 0.29763018,
      "mean": 0.13177108533333334,
      "min": 0.046235376,
      "p50": 0.051447700000000006,
      "p75": 0.29763018,
      "p95": 0.29763018,
      "p98": 0.29763018,
      "p99": 0.29763018,
      "p999": 0.29763018,
      "stddev": 0.14366183050172013,
      "m15_rate": 0.17327375280520316,
      "m1_rate": 0.033814570567886885,
      "m5_rate": 0.1309493035278718,
      "mean_rate": 0.020034416530604948,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    },
    "rundeck.web.requests.requestTimer": {
      "count": 5,
      "max": 0.19833273,
      "mean": 0.0921360348,
      "min": 0.008643088,
      "p50": 0.106350626,
      "p75": 0.16599702400000002,
      "p95": 0.19833273,
      "p98": 0.19833273,
      "p99": 0.19833273,
      "p999": 0.19833273,
      "stddev": 0.08113047507342931,
      "m15_rate": 0.33800395660416016,
      "m1_rate": 0.05334571472303017,
      "m5_rate": 0.24315644263411573,
      "mean_rate": 0.02965980629218819,
      "duration_units": "seconds",
      "rate_units": "calls/second"
    }
  }
}'''
        ).addHeader('content-type', 'application/json')
        );
        server.start()

        def retrofit = new Retrofit.Builder().baseUrl(server.url('/api/25/')).
                addConverterFactory(JacksonConverterFactory.create()).
                build()
        def api = retrofit.create(RundeckApi)

        when:
        def body = api.getMetricsData().execute().body()

        then:
        RecordedRequest request1 = server.takeRequest()
        request1.path == '/api/25/metrics/metrics'

        body.version == "3.1.3"
        body.gauges.size() == 27
        body.gauges."rundeck.services.AuthorizationService.sourceCache.hitCount".value == 9
        body.gauges."rundeck.services.AuthorizationService.sourceCache.hitRate".value == 0.9
        body.gauges."rundeck.services.NodeService.nodeCache.hitRate".value == 0.9166666666666666
        body.counters.size() == 1
        body.counters."rundeck.scheduler.quartz.scheduledJobs".count == 6
        body.histograms.size() == 0
        body.meters.size() == 5
        body.meters."rundeck.services.AuthorizationService.systemAuthorization.evaluateMeter".count == 4
        body.meters."rundeck.services.AuthorizationService.systemAuthorization.evaluateMeter".m1_rate == 0.023875601445527157
        body.meters."rundeck.services.AuthorizationService.systemAuthorization.evaluateMeter".units == "events/second"
        body.timers.size() == 9
        body.timers."rundeck.services.AuthorizationService.getSystemAuthorization".count == 10
        body.timers."rundeck.services.AuthorizationService.getSystemAuthorization".mean == 0.0168355508
        body.timers."rundeck.services.AuthorizationService.getSystemAuthorization".duration_units == "seconds"
        server.shutdown()
    }

}
