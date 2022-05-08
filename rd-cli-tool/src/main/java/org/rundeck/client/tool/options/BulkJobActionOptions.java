package org.rundeck.client.tool.options;

import lombok.Data;
import picocli.CommandLine;

@Data
public class BulkJobActionOptions extends JobListOptions {

  @CommandLine.Option(names={"--confirm","-y"}, description = "Force confirmation of request.")
  boolean confirm;

}
