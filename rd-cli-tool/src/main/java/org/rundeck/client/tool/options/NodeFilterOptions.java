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

package org.rundeck.client.tool.options;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

import java.util.List;

/**
 * @author greg
 * @since 11/30/16
 */
@Getter @Setter
public class NodeFilterOptions extends NodeFilterBaseOptions{

    @CommandLine.Parameters(paramLabel = "NODE FILTER", description = "Node filter")
    List<String> filterTokens;

    public boolean isFilterTokens() {
        return filterTokens != null && !filterTokens.isEmpty();
    }

    public String filterString() {
        if (isFilter()) {
            return getFilter();
        } else if (isFilterTokens()) {
            return String.join(" ", getFilterTokens());
        }
        return null;
    }
}
