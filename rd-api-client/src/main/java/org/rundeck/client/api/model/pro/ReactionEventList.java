package org.rundeck.client.api.model.pro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.rundeck.client.api.model.Paging;
import org.rundeck.client.util.DataOutput;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReactionEventList
        implements DataOutput
{
    protected List<ReactionEvent> events;
    protected Paging pagination;

    public List<?> asList() {
        return DataOutput.collectList(events);
    }
}
