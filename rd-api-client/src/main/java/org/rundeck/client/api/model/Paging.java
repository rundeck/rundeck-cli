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

/**
 * Paging data
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Paging {
    private int count;
    private int total;
    private int max;
    private int offset;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format(
                "Page [%d/%d] results %d - %d (of %d by %d)",
                pagenum(),
                maxPagenum(),
                offset + 1,
                count + offset,
                total,
                max
        );
    }


    public String moreResults(final String offsetArg) {
        return moreResults(offsetArg, null);
    }

    public String moreResults(final String offsetArg, final String extra) {
        if (hasMoreResults()) {
            int nextOffset = getOffset() + getMax();
            return String.format(
                    "(more results available, append: %s %d%s)",
                    offsetArg,
                    nextOffset,
                    null != extra ? extra : ""
            );
        } else {
            return "End of results.";
        }
    }

    public int pagenum() {
        if (max < 1) {
            return 1;
        }

        int oflow = offset % max;
        return 1 + (offset - oflow) / max + (oflow > 0 ? 1 : 0);
    }

    public int maxPagenum() {
        if (max < 1) {
            return 1;
        }
        int oflow = total % max;
        return (total - oflow) / max + (oflow > 0 ? 1 : 0);

    }

    public int nextPageOffset() {
        if (!hasMoreResults()) {
            return -1;
        }
        return offset + count;
    }
    public boolean hasMoreResults() {
        return getTotal() > (getOffset() + getCount());
    }
}
