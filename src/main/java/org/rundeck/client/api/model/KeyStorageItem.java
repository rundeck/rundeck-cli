/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
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

package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Created by greg on 6/6/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyStorageItem implements Comparable<KeyStorageItem>{
    public static final String RUNDECK_DATA_TYPE = "Rundeck-data-type";
    public static final String RUNDECK_KEY_TYPE = "Rundeck-key-type";
    public static final String PASSWORD_DATA_TYPE = "password";
    public static final String PRIVATE_KEY_TYPE = "private";
    public static final String PUBLIC_KEY_TYPE = "public";
    private String path;
    private KeyItemType type;
    private String name;
    private String url;
    private Map<String, String> meta;
    private List<KeyStorageItem> resources;
    private Object metaString;
    private String fileType;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public KeyItemType getType() {
        return type;
    }

    public void setType(KeyItemType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    public List<KeyStorageItem> getResources() {
        return resources;
    }

    public void setResources(List<KeyStorageItem> resources) {
        this.resources = resources;
    }

    public String getMetaString() {
        return getMetaString("");
    }
    public String getMetaString(String prefix) {
        StringBuilder sb = new StringBuilder();
        if (null != getMeta() && getMeta().size() > 0) {
            getMeta().keySet().forEach(m -> sb.append(prefix).append(m).append(": ").append(getMeta().get(m)).append("\n"));
        }
        return sb.toString();
    }

    public KeyFileType getFileType() {
        if (null != getMeta()) {
            if (PASSWORD_DATA_TYPE.equals(getMeta().get(RUNDECK_DATA_TYPE))) {
                return KeyFileType.password;
            } else if (PRIVATE_KEY_TYPE.equals(getMeta().get(RUNDECK_KEY_TYPE))) {
                return KeyFileType.privateKey;
            } else if (PUBLIC_KEY_TYPE.equals(getMeta().get(RUNDECK_KEY_TYPE))) {
                return KeyFileType.publicKey;
            }
        }
        return KeyFileType.other;
    }

    public static enum KeyFileType {
        password,
        privateKey,
        publicKey,
        other
    }

    public static enum KeyItemType {
        directory,
        file
    }

    public String toBasicString() {
        boolean isDir = isDirectory();
        if (isDir) {
            return String.format("%s/",  path);
        } else {
            return String.format("%s [%s]",  path, getFileType());
        }
    }

    public boolean isDirectory() {
        return type == KeyItemType.directory;
    }

    @Override
    public String toString() {
        return "org.rundeck.client.api.model.KeyStorageItem{" +
               "path='" + path + '\'' +
               ", type=" + type +
               ", name='" + name + '\'' +
               ", url='" + url + '\'' +
               ", meta=" + meta +
               ", resources=" + resources +
               '}';
    }

    @Override
    public int compareTo(final KeyStorageItem o) {
        if(isDirectory() && !o.isDirectory()){
            return -1;
        }else if(!isDirectory() && o.isDirectory()){
            return 1;
        }
        return getPath().compareTo(o.getPath());
    }
}
