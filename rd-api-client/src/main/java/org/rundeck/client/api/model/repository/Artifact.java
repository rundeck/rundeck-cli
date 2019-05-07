/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rundeck.client.api.model.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifact {
    String       id;
    String       name;
    String       description;
    String       installId;
    String       author;
    String       rundeckCompatibility;
    String       targetHostCompatibility;
    String       currentVersion;
    boolean      installed;
    boolean      updatable;
    String       installedVersion;
    List<String> oldVersions;
    List<String> tags;
    List<String> providesServices;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getInstallId() { return installId; }

    public void setInstallId(final String installId) { this.installId = installId; }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public String getRundeckCompatibility() {
        return rundeckCompatibility;
    }

    public void setRundeckCompatibility(final String rundeckCompatibility) {
        this.rundeckCompatibility = rundeckCompatibility;
    }

    public String getTargetHostCompatibility() {
        return targetHostCompatibility;
    }

    public void setTargetHostCompatibility(final String targetHostCompatibility) {
        this.targetHostCompatibility = targetHostCompatibility;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(final String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public boolean isInstalled() { return installed; }

    public void setInstalled(final boolean installed) {
        this.installed = installed;
    }

    public boolean isUpdatable() { return updatable; }

    public String getInstalledVersion() { return installedVersion; }

    public List<String> getOldVersions() {
        return oldVersions;
    }

    public void setOldVersions(final List<String> oldVersions) {
        this.oldVersions = oldVersions;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public List<String> getProvidesServices() {
        return providesServices;
    }

    public void setProvidesServices(final List<String> providesServices) {
        this.providesServices = providesServices;
    }
}
