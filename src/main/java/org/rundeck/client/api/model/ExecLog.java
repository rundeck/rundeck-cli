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
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecLog {
    public String time;
    public String level;
    public String log;
    public String user;
    public String command;
    public String node;
    public String stepctx;


    public ExecLog() {

    }

    public ExecLog(final String log) {
        this.log = log;
    }

    public ExecLog decompact(ExecLog prev) {
        ExecLog clone = new ExecLog();
        clone.time = null == time && null != prev ? prev.time : time;
        clone.level = null == level && null != prev ? prev.level : level;
        clone.log = null == log && null != prev ? prev.log : log;
        clone.user = null == user && null != prev ? prev.user : user;
        clone.command = null == command && null != prev ? prev.command : command;
        clone.node = null == node && null != prev ? prev.node : node;
        clone.stepctx = null == stepctx && null != prev ? prev.stepctx : stepctx;
        return clone;
    }
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("time", time);
        map.put("level", level);
        map.put("log", log);
        map.put("user", user);
        map.put("command", command);
        map.put("node", node);
        map.put("stepctx", stepctx);
        return map;
    }
}
