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

import org.rundeck.toolbelt.CommandOutput
import org.rundeck.client.api.RundeckApi
import org.rundeck.client.tool.RdApp
import org.rundeck.client.util.Client
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import org.rundeck.client.api.model.User


class UsersSpec extends Specification {

    def "info correct endpoint redirect"() {
        given:

        def api = Mock(RundeckApi)
        def opts = Mock(Users.Info) {
            getLogin() >> username
            isLogin() >> (username != null)
        }

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 21, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Users users = new Users(hasclient)
        def out = Mock(CommandOutput)

        when:
        users.info(opts,out)

        then:
        sameUserEndpoint * api.getUserInfo() >>
                Calls.response(
                        new User(login: 'login', email: 'test@email.com')
                )
        otherUserEndpoint * api.getUserInfo(username) >>
                Calls.response(
                        new User(login: username, email: 'test@email.com')
                )
        1 * out.output(result)


        where:
        username            | result            |sameUserEndpoint | otherUserEndpoint
        null                | 'Login: [login]'  | 1               |0
        'admin'             | 'Login: [admin]'  | 0               |1

    }

    def "edit correct endpoint redirect"() {
        given:

        def api = Mock(RundeckApi)
        def opts = Mock(Users.Edit) {
            getLogin() >> username
            isLogin() >> (username != null)
            getEmail() >> 'test@email.com'
            isEmail() >> true
        }

        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 21, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Users users = new Users(hasclient)
        def out = Mock(CommandOutput)

        when:
        users.edit(opts,out)

        then:
        sameUserEndpoint * api.editUserInfo(_) >>
                Calls.response(
                        new User(login: 'login', email: 'test@email.com')
                )
        otherUserEndpoint * api.editUserInfo(username,_) >>
                Calls.response(
                        new User(login: username, email: 'test@email.com')
                )
        1 * out.output(result)


        where:
        username            | result            |sameUserEndpoint | otherUserEndpoint
        null                | 'Login: [login]'  | 1               |0
        'admin'             | 'Login: [admin]'  | 0               |1

    }

    def "list correct execution"() {
        given:

        def api = Mock(RundeckApi)


        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, 21, true, null)
        def hasclient = Mock(RdApp) {
            getClient() >> client
        }
        Users users = new Users(hasclient)
        def out = Mock(CommandOutput){
            output(_) >>{msg->println(msg)}
        }
        List<User> arr = new ArrayList<User>()
        if(userCount>0){
            (1..userCount).each{
                arr.push(new User(login: 'login', email: 'test@email.com'))
            }
        }
        def opt = Mock(Users.ListOption)

        when:
        users.list(opt, out)

        then:
        1 * api.listUsers() >>
                Calls.response(
                        arr
                )
        1 * out.info(userCount+' Users:')
        userCount * out.output(_)


        where:
        userCount   | _
        1           | _
        3           | _
        0           | _

    }
}
