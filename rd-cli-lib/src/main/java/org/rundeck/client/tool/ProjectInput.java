package org.rundeck.client.tool;

public interface ProjectInput {
    String PROJECT_NAME_PATTERN = "^[-_a-zA-Z0-9+][-\\._a-zA-Z0-9+]*$";
    String getProject();
}
