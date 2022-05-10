package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
public class BulkJobDelete {

    private List<String> ids;

    @JsonCreator
    public BulkJobDelete(final List<String> ids) {
        this.ids = ids;
    }
}
