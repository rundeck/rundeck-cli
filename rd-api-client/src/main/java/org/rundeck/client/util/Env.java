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

package org.rundeck.client.util;

/**
 * Configuration values from System.getenv,
 * keys will be converted to upper case, and "." replaced with "_".
 */
public class Env extends ConfigBase implements ConfigSource {
    public String getString(final String key, final String defval) {
        String val = System.getenv(key.toUpperCase().replaceAll("\\.", "_"));
        if (val != null) {
            return val;
        } else {
            return defval;
        }
    }
}
