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

/**
 * @author greg
 * @since 3/2/17
 */
@Getter @Setter
public class PagingResultOptions {

    @CommandLine.Option(names = {"-m", "--max"}, description = "Maximum number of results to retrieve at once.")
    private Integer max;

    public boolean isMax() {
        return max != null && max > 0;
    }

    @CommandLine.Option(names = {"-o", "--offset"}, description = "First result offset to receive.")
    private Integer offset;

    public boolean isOffset() {
        return offset != null && offset > 0;
    }
}
