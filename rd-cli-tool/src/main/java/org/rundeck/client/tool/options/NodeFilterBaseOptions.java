package org.rundeck.client.tool.options;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

@Getter
@Setter
public class NodeFilterBaseOptions {
    @CommandLine.Option(names = {"-F", "--filter"}, description = "A node filter string")
    private String filter;

    public boolean isFilter() {
        return filter != null;
    }
}
