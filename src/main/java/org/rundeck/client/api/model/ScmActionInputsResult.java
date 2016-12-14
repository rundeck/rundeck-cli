package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author greg
 * @since 12/13/16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScmActionInputsResult {
    public String title;
    public String description;
    public String integration;
    public String actionId;
    public List<ScmInputField> fields;
    public List<ScmImportItem> importItems;
    public List<ScmExportItem> exportItems;
}
