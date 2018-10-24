package org.rundeck.client.api.model.pro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.rundeck.client.api.model.Paging;
import org.rundeck.client.util.DataOutput;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReactionList implements DataOutput {
    private Paging paging;
    private List<Reaction> reactions;

    public List<?> asList() {
        return getReactions().stream().map(Reaction::asMap).collect(Collectors.toList());
    }
}
