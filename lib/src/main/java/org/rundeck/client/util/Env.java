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
 * Configuration values from System.getenv
 */
public class Env implements ConfigSource {

    public int getInt(final String debug, final int defval) {
        String envProp = getString(debug, null);
        if (null != envProp) {
            return Integer.parseInt(envProp);
        } else {
            return defval;
        }
    }

    public Long getLong(final String key, final Long defval) {
        String timeoutEnv = getString(key, null);
        if (null != timeoutEnv) {
            return Long.parseLong(timeoutEnv);
        } else {
            return defval;
        }
    }

    public boolean getBool(final String key, final boolean defval) {
        return "true".equalsIgnoreCase(getString(key, defval ? "true" : "false"));
    }

    public String getString(final String key, final String defval) {
        String val = System.getenv(key);
        if (val != null) {
            return val;
        } else {
            return defval;
        }
    }

    @Override
    public String get(final String key) {
        return getString(key, null);
    }

    public String require(final String name, final String description)  throws ConfigSourceError {
        String value = System.getenv(name);
        if (null == value) {
            throw new ConfigSourceError(String.format(
                    "Environment variable %s is required: %s",
                    name,
                    description
            ));
        }
        return value;
    }

}
