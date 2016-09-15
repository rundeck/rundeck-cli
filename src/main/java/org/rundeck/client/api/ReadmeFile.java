package org.rundeck.client.api;

/**
 * Created by greg on 9/15/16.
 */
public enum ReadmeFile {
    README("readme.md"),
    MOTD("motd.md");

    String name;

    ReadmeFile(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
