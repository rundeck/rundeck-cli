package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 5/20/16.
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
        return String.format("Paged results %d - %d (of %d by %d).", offset, count + offset, total, max);
    }

    public String moreResults(final String offsetArg) {
        if (hasMoreResults()) {
            int nextOffset = getOffset() + getMax();
            return String.format("(more results available, append: %s %d)", offsetArg, nextOffset);
        } else {
            return "End of results.";
        }
    }

    public boolean hasMoreResults() {
        return getTotal() > (getOffset() + getCount());
    }
}
