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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiToken {
    private String id;
    private String token;
    private String user;
    private String creator;
    private List<String> roles;
    private Boolean expired;
    private DateInfo expiration;

    @Override
    public String toString() {
        return "API Token: " + getTruncatedIdOrToken();
    }
    public String toFullString() {
        return "API Token: " + id;
    }


    public String getTruncatedId() {
        return id != null ? id.substring(0, 5) + "*****" : null;
    }

    public String getTruncatedIdOrToken() {
        return id != null ? id.substring(0, 5) + "*****" : null != token ? token.substring(0, 5) + "*****" : "?";
    }

    public String getId() {
        return id;
    }

    public String getIdOrToken() {
        return id != null ? id : token != null ? token : "?";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user", getUser());
        if (null != getRoles() && !getRoles().isEmpty()) {
            map.put("roles", getRoles());
        }
        if (null != getId()) {
            map.put("id", getId());
        }
        if (null != getToken()) {
            map.put("token", getToken());
        }
        if (null != getCreator()) {
            map.put("creator", getCreator());
        }
        if (null != getExpiration()) {
            map.put("expiration", getExpiration().date);
        }
        if (null != getExpired()) {
            map.put("expired", getExpired());
        }
        return map;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public DateInfo getExpiration() {
        return expiration;
    }

    public void setExpiration(DateInfo expiration) {
        this.expiration = expiration;
    }
}
