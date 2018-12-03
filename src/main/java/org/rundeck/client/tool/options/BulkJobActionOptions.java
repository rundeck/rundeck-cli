package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

public interface BulkJobActionOptions extends JobListOptions {

  @Option(longName = "confirm", shortName = "y", description = "Force confirmation of request.")
  boolean isConfirm();

}
