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
package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String login;
    private String firstName;
    private String lastName;
    private String email;


    @Override
    public String toString() {
        return String.format(
                "%s %s %s %s",
                login,
                firstName,
                lastName,
                email
        );
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean hasFirstName(){
        return hasValue(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean hasLastName(){
        return hasValue(lastName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasEmail(){
        return hasValue(email);
    }

    private boolean hasValue(String text){
        return (null != text && text.length()>0);
    }

    public String toBasicString(){
        return String.format("%s: name [%s], last Name [%s], email [%s]",login,hasFirstName()?firstName:"",
                hasLastName()?lastName:"",hasEmail()?email:"");
    }
}
