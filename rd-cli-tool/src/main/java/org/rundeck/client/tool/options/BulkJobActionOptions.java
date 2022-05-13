package org.rundeck.client.tool.options;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

@Getter @Setter
public class BulkJobActionOptions extends JobListOptions {

  @CommandLine.Option(names={"--confirm","-y"}, description = "Force confirmation of request.")
  boolean confirm;

}
