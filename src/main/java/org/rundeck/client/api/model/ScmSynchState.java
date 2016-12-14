package org.rundeck.client.api.model;

/**
 * @author greg
 * @since 12/13/16
 */
public enum ScmSynchState {
    CLEAN,
    UNKNOWN,
    REFRESH_NEEDED,
    IMPORT_NEEDED,
    EXPORT_NEEDED,
    DELETE_NEEDED,
    CREATE_NEEDED
}
