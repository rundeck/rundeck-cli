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
 * @author greg
 * @since 1/11/17
 */
public class ExtConfigSource
        extends ConfigBase
        implements ConfigSource
{
    final ConfigSource configSource;

    public ExtConfigSource(final ConfigSource configSource) {
        this.configSource = configSource;
    }

    @Override
    public String getString(final String key, final String defval) {
        return configSource.getString(key, defval);
    }
}
