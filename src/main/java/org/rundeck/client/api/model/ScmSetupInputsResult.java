package org.rundeck.client.api.model;

import java.util.List;

/**
 * @author greg
 * @since 12/13/16
 */
public class ScmSetupInputsResult {
    public String integration;
    public String type;
    public List<ScmInputField> fields;
}
