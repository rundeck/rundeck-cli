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

import org.rundeck.client.api.model.RoleList

import org.rundeck.client.api.RundeckApi
import org.rundeck.client.testing.MockRdTool
import org.rundeck.client.tool.CommandOutput
import org.rundeck.client.tool.RdApp
import org.rundeck.client.tool.extension.RdTool
import org.rundeck.client.tool.options.LoginNameOption
import org.rundeck.client.tool.options.UserFormatOption
import org.rundeck.client.util.Client
import org.rundeck.client.util.RdClientConfig
import retrofit2.Retrofit
import retrofit2.mock.Calls
import spock.lang.Specification
import org.rundeck.client.api.model.User


class UsersSpec extends Specification {

    private RdTool setupMock(RundeckApi api, int apiVersion = 18) {
        def retrofit = new Retrofit.Builder().baseUrl('http://example.com/fake/').build()
        def client = new Client(api, retrofit, null, null, apiVersion, true, null)
        def rdapp = Mock(RdApp) {
            getClient() >> client
            getAppConfig() >> Mock(RdClientConfig)
        }
        def rdTool = new MockRdTool(client: client, rdApp: rdapp)
        rdTool.appConfig = Mock(RdClientConfig)
        rdTool
    }

    def "info correct endpoint redirect"() {
        given:

        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 21)
        def out = Mock(CommandOutput)
        Users command = new Users()
        command.rdTool = rdTool
        command.rdOutput = out

        def opts = new LoginNameOption()
        opts.login=username

        when:
        command.info(opts, new UserFormatOption())

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
        RdTool rdTool = setupMock(api, 21)
        def out = Mock(CommandOutput)
        Users command = new Users()
        command.rdTool = rdTool
        command.rdOutput = out

        def opts = new Users.Edit()
        opts.login=username
        opts.email='test@email.com'


        when:
        command.edit(opts, new UserFormatOption())

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
        RdTool rdTool = setupMock(api, 21)
        def out = Mock(CommandOutput)
        Users command = new Users()
        command.rdTool = rdTool
        command.rdOutput = out

        List<User> arr = new ArrayList<User>()
        if(userCount>0){
            (1..userCount).each{
                arr.push(new User(login: 'login', email: 'test@email.com'))
            }
        }

        when:
        command.list(new UserFormatOption())

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

    def "list roles apiv30"() {
        given:
        def api = Mock(RundeckApi)
        RdTool rdTool = setupMock(api, 30)
        def out = Mock(CommandOutput)
        Users command = new Users()
        command.rdTool = rdTool
        command.rdOutput = out


        RoleList roleList = new RoleList();
        roleList.roles = Arrays.asList("admin","user");

        when:
        command.roles()

        then:
        1 * api.listRoles() >>
        Calls.response(
                roleList
        )
        2 * out.output(_)
    }
}
