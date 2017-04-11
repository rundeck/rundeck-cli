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

import com.simplifyops.toolbelt.InputError;

/**
 * @author greg
 * @since 1/11/17
 */
public interface ConfigSource {

    int getInt(final String key, final int defval);

    Long getLong(final String key, final Long defval);

    boolean getBool(final String key, final boolean defval);

    String getString(final String key, final String defval);

    String get(final String key);

    String require(final String key, final String description) throws InputError;

}
