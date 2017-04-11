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

package org.rundeck.client.util

import spock.lang.Specification

/**
 * Created by greg on 9/9/16.
 */
class RedirectBypassInterceptorSpec extends Specification {
    def "remap"() {
        when:
        String result = RedirectBypassInterceptor.remapUrl(orig, bypass, app)


        then:
        result == expect

        where:
        orig                    | bypass            | app               | expect
        "http://host1/c1/path1" | "http://host1"    | "http://host2/c2" | "http://host2/c2/c1/path1"
        "http://host1/c1/path1" | "http://host1/c1" | "http://host2/c2" | "http://host2/c2/path1"
        "http://host1/c1/path1" | "http://host1/c1" | "http://host2"    | "http://host2/path1"
    }
}
